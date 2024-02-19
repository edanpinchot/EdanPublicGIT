package edu.yu.oatsdb.v2;

import edu.yu.oatsdb.base.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;


public enum TxMgrImpl implements TxMgr {
    Instance;
    private TxImpl transaction = new TxImpl();
    private Thread thread = Thread.currentThread();
    private Map<Thread, TxImpl> ThreadToTx = new HashMap<>();
    private FileOutputStream fos = null;
    private ObjectOutputStream oos;
    private String path = System.getProperty("user.home");


    public void begin() throws NotSupportedException, SystemException {
        //if thread is already associated with a transaction
        if (ThreadToTx.get(thread) != null) {
            throw new NotSupportedException("ERROR: Thread is already associated with a transaction.");
        }

        transaction.setStatuses(TxStatus.ACTIVE);

        //and associate transaction with the current thread
        ThreadToTx.put(thread, transaction);

        try {
            String path = System.getProperty("user.home");
            Path p = Paths.get(path + "/MapFiles");
            if (Files.exists(p)) {
                FileInputStream fis = new FileInputStream(path + "/MapFiles/mapNames.txt");
                ObjectInputStream ois = new ObjectInputStream(fis);
                boolean read = true;
                while (read) {
                    try {
                        String mapName = null;
                        mapName = (String) ois.readObject();

                        int i = deserializePointerFile(mapName);
                        if (i == 0) {
                            DBMSImpl.MyMap map = deserialize(mapName, 0);
                            DBMSImpl.Instance.createAnew(mapName, map);
                        } else {
                            DBMSImpl.MyMap map = deserialize(mapName, 1);
                            DBMSImpl.Instance.createAnew(mapName, map);
                        }
                    } catch (EOFException e) {
                        read = false;
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

//        if (DBMSImpl.Instance.getMapContainer().isEmpty()) {
//            try {
//                String path = System.getProperty("user.home");
//                File theDir = new File(path + "/MapFiles");
//                if (!theDir.exists()) {
//                    theDir.mkdirs();
//                }
//                File file = new File(path + "/MapFiles/mapNames.txt");
//                file.createNewFile();
//
//                Scanner scanner = new Scanner(new File(path + "/MapFiles/mapNames.txt"));
//                while (scanner.hasNextLine()) {
//                    String mapName = scanner.nextLine();
//                    int i = deserializePointerFile(mapName);
//                    if (i == 0) {
//                        DBMSImpl.MyMap map = deserialize(mapName, 0);
//                        DBMSImpl.Instance.createAnew(mapName, map);
//                    } else {
//                        DBMSImpl.MyMap map = deserialize(mapName, 1);
//                        DBMSImpl.Instance.createAnew(mapName, map);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

//        //rebuild the DBMS from disk
//        Map<String, DBMSImpl.MyMap> mapContainer = DBMSImpl.Instance.getMapContainer();
//        for (String mapName : mapContainer.keySet()) {
//            int i = deserializePointerFile(mapName);
//            if (i == 0) {
//                DBMSImpl.MyMap map = deserialize(mapName, 0);
//                DBMSImpl.Instance.createAnew(mapName, map);
//            }
//            else {
//                DBMSImpl.MyMap map = deserialize(mapName, 1);
//                DBMSImpl.Instance.createAnew(mapName, map);
//            }
//        }
    }


    public void commit() throws RollbackException, IllegalStateException, SystemException {
        //indicate that transaction has been rolled back instead of committed
        if (transaction.getStatus().equals(TxStatus.ROLLEDBACK)) {
            throw new RollbackException("ERROR: Transaction has been rolled back rather than committed.");
        }

        //if the current thread is not associated with a transaction
        transactionAssociation();

        transaction.setStatuses(TxStatus.COMMITTING);

        //thread is no longer associated with the transaction
        ThreadToTx.remove(thread);

        //transfer everything in shadow to actual, then release locks
        for (MapAndShadow ms : DBMSImpl.Instance.getShadowsMapped()) {
            DBMSImpl.MyMap actualMap = ms.getActualMap();
            DBMSImpl.MyMap shadowMap = ms.getShadowMap();
            actualMap.putAll(shadowMap);

            Map<Object, Lock> lockedEntries = shadowMap.getLockedEntries();
            for(Lock lock : lockedEntries.values()) {
                lock.unlock();
            }
            lockedEntries.clear();
        }

        //for each map in the database, write it to disk
        Map<String, DBMSImpl.MyMap> mapContainer = DBMSImpl.Instance.getMapContainer();
        for (String mapName : mapContainer.keySet()) {
            DBMSImpl.MyMap map = mapContainer.get(mapName);
            serialize(map, mapName, map.getKeyClass(), map.getValueClass());
        }

        transaction.setStatuses(TxStatus.COMMITTED);

        //make _pointer.txt point to newest map
        try {
            for (String mapName : mapContainer.keySet()) {
                int i = deserializePointerFile(mapName);
                if (i == 0) {
                    fos = new FileOutputStream(path + "/MapFiles/" + mapName + "_pointer.txt");
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(1);
                }
                else {
                    fos = new FileOutputStream(path + "/MapFiles/" + mapName + "_pointer.txt");
                    oos = new ObjectOutputStream(fos);
                    oos.writeObject(0);
                }
            }

            fos.close();
            oos.close();
            DBMSImpl.Instance.commitStreamCloser();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void rollback() throws IllegalStateException, SystemException {
        //if the current thread is not associated with a transaction
        transactionAssociation();

        transaction.setStatuses(TxStatus.ROLLING_BACK);

        //thread is no longer associated with the transaction
        ThreadToTx.remove(thread);

        //discard the shadow by resetting it to the actual, then release locks
        for (MapAndShadow ms : DBMSImpl.Instance.getShadowsMapped()) {
            DBMSImpl.MyMap actualMap = ms.getActualMap();
            DBMSImpl.MyMap shadowMap = ms.getShadowMap();
            shadowMap.clear();
            shadowMap.putAll(actualMap);

            Map<Object, Lock> lockedEntries = shadowMap.getLockedEntries();
            for(Lock lock : lockedEntries.values()) {
                lock.unlock();
            }
            lockedEntries.clear();
        }

        transaction.setStatuses(TxStatus.ROLLEDBACK);
    }


    public Tx getTx() throws SystemException {
        //if no transaction is associated with current thread, return a transaction with TxStatus "NO_TRANSACTION"
        if ((!ThreadToTx.containsKey(thread)) || (ThreadToTx.get(thread) == null)) {
            transaction.setStatuses(TxStatus.NO_TRANSACTION);
        }

        return transaction;
    }


    public TxStatus getStatus() throws SystemException {
        return transaction.getStatus();
    }


    public void transactionAssociation() {
        TxStatus status = null;
        try {
            //status will be NO_TRANSACTION as long as this thread is not associated with a transaction, which includes its status being UNKOWN, COMMITTED, or ROLLEDBACK
            status = getTx().getStatus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (status == (TxStatus.NO_TRANSACTION)) {
            throw new IllegalStateException("ERROR: Current thread is not associated with a transaction.");
        }
    }


    private void serialize(DBMSImpl.MyMap map, String mapName, Class keyClass, Class valueClass) {
        try {
            //deserialize pointer.txt - serialize this map to whichever file it DOESNT point to
            int i = deserializePointerFile(mapName);
            if (i == 0) {
                fos = new FileOutputStream(path + "/MapFiles/" + mapName + "_1.txt");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(map);
            }
            else {
                fos = new FileOutputStream(path + "/MapFiles/" + mapName + "_0.txt");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(map);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DBMSImpl.MyMap deserialize(String mapName, int i) {
        DBMSImpl.MyMap map = null;
        try {
            FileInputStream fis = new FileInputStream(path + "/MapFiles/" + mapName + "_" + i + ".txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (DBMSImpl.MyMap) ois.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public int deserializePointerFile(String mapName) {
        int i = 0;
        try {
            FileInputStream fis = new FileInputStream(path + "/MapFiles/" + mapName + "_pointer.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            i = (int) ois.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return i;
    }
}
