package edu.yu.oatsdb.v1;

import edu.yu.oatsdb.base.*;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;


public enum TxMgrImpl implements TxMgr {
    Instance;
    private TxImpl transaction = new TxImpl();
    private Thread thread = Thread.currentThread();
    private Map<Thread, TxImpl> ThreadToTx = new HashMap<>();


    public void begin() throws NotSupportedException, SystemException {
        //if thread is already associated with a transaction
        if (ThreadToTx.get(thread) != null) {
            throw new NotSupportedException("ERROR: Thread is already associated with a transaction.");
        }

        transaction.setStatuses(TxStatus.ACTIVE);

        //and associate transaction with the current thread
        ThreadToTx.put(thread, transaction);
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

        //transfers everything done from shadow database
        deserialize();
        DBMSImpl.Instance.commitStreamCloser();


        transaction.setStatuses(TxStatus.COMMITTED);
    }


    public void rollback() throws IllegalStateException, SystemException {
        //if the current thread is not associated with a transaction
        transactionAssociation();

        transaction.setStatuses(TxStatus.ROLLING_BACK);

        //thread is no longer associated with the transaction
        ThreadToTx.remove(thread);

        //discard the shadow database, aka empty out shadowLog.txt file by resetting it
        DBMSImpl.Instance.refreshFos();



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


    public void deserialize() {
        try {
            FileInputStream fis = new FileInputStream("shadowLog.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            boolean read = true;
            while (read) {
                try {
                    MapObject obj = null;
                    obj = (MapObject) ois.readObject();
                    shadowOperations(obj);
                    //if this is a call to "remove", so no need to unlock the entry - its gone already
                    if (!(obj.getMethodName().equals("remove"))) {
                        unlocks(obj);
                    }
                }
                catch (EOFException e) {
                    read = false;
                }
            }
            ois.close();
            fis.close();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void rollbackDeserialize() {
        try {
            FileInputStream fis = new FileInputStream("shadowLog.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            boolean read = true;
            while (read) {
                try {
                    MapObject obj = null;
                    obj = (MapObject) ois.readObject();
                    if (!(obj.getMethodName().equals("remove"))) {
                        unlocks(obj);
                    }
                }
                catch (EOFException e) {
                    read = false;
                }
            }
            ois.close();
            fis.close();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void shadowOperations(MapObject obj) {
        DBMSImpl.MyMap map = DBMSImpl.Instance.getMapContainer().get(obj.getMapName());

        if (obj.getMethodName().equals("put")) {
            //i need this to be able to retrieve the MyMap that this "put" was done to initially...
            map.commitPut(obj.getKey(), obj.getValue());
        }
        if (obj.getMethodName().equals("remove")) {
            map.commitRemove(obj.getKey());
        }
    }


    public void unlocks(MapObject obj) {
        DBMSImpl.MyMap map = DBMSImpl.Instance.getMapContainer().get(obj.getMapName());

        Lock lock = (Lock) map.getLockedEntries().get(obj.getKey());
        lock.unlock();
    }
}