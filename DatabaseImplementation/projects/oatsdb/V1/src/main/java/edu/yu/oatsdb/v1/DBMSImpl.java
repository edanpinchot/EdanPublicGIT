package edu.yu.oatsdb.v1;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.yu.oatsdb.base.*;


public enum DBMSImpl implements DBMS, ConfigurableDBMS  {
    Instance;
    //need a Map to serve as the container of containers - associates Map Name with arbitrary generic (not raw) Map instances
    private Map<String, MyMap> mapContainer = new HashMap<>();
    private Map<String, keyValue> keyValueContainer = new HashMap<>();
    private FileOutputStream fos = null;
    private ObjectOutputStream oos;
    private int TxTimeoutInMillis = 0;


    public class MyMap<K, V> extends HashMap implements java.io.Serializable {
        private Map<K, Lock> lockedEntries = new HashMap<>();
        private String name;
        private Class<K> keyClass;
        private Class<V> valueClass;

        public MyMap(String name, Class<K> keyClass, Class<V> valueClass) {
            this.name = name;
            this.keyClass = keyClass;
            this.valueClass = valueClass;
        }

        public String getName() {
            return name;
        }

        @Override
        public Object put(Object key, Object value) {
            transactionAssociation();

            if (key == null) {
                throw new IllegalArgumentException("ERROR: Keys cannot be null");
            }

            if (key.getClass() != keyValueContainer.get(this.name).getKey() || value.getClass() != keyValueContainer.get(this.name).getValue()) {
                throw new ClassCastException("ERROR: Incorrect key class or value class.");
            }

            if (!(value instanceof Serializable)) {
                throw new IllegalArgumentException("Value inserted is not serializable");
            }

            //serialize object into file - then in commit() method, deserialize and use 'commitPut' to put into the actual map
            MapObject obj = new MapObject("put", key, value, this.name);
            serialize(obj);

            //lock this map entry
            lock((K) key);

            return value;
        }

        @Override
        public Object get(Object key) {
            transactionAssociation();

            //no serialization for a "get" - just deserialize until the "put" for this map is found, and return the value
            //on second thought - need to serialize "get" so that the lock on this map will be undone
            Object value = deserialize(key, this);
            MapObject obj = new MapObject("get", key, value, this.name);
            serialize(obj);

            //lock this map entry
            lock((K) key);

            return value;
        }

        @Override
        public Object remove(Object key) {
            transactionAssociation();

            //serialize
            MapObject obj = new MapObject("remove", key, null, this.name);
            serialize(obj);

            return removeDeserialize(key, this);
        }

        public Object commitPut(Object key, Object value) {
            return super.put(key, value);
        }

        public Object commitRemove(Object key) {
            return super.remove(key);
        }

        public Map<K, Lock> getLockedEntries() {
            return lockedEntries;
        }

        public void lock(K key) {
            try {
                if (lockedEntries.containsKey(key)) {
                    lockedEntries.get(key).tryLock(TxTimeoutInMillis, TimeUnit.MILLISECONDS);
                }
                else {
                    Lock lock = new ReentrantLock();
                    lock.tryLock(TxTimeoutInMillis, TimeUnit.MILLISECONDS);
                    //cast
                    lockedEntries.put((K) key, lock);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    public Map<String, MyMap> getMapContainer() {
        return mapContainer;
    }


    public void refreshFos() {
        try {
            fos = new FileOutputStream("shadowLog.txt");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    public <K, V> Map<K, V> getMap(final String name, Class<K> keyClass, Class<V> valueClass) {

        //if client is not associated with a transaction
        transactionAssociation();

        //pull the map with this "name" from our container
        Map<K, V> map = mapContainer.get(name);

        //if no map is associated with the specified name
        if (map == null) {
            throw new NoSuchElementException("ERROR: No map is associated with the specified name.");
        }

        //throw an error if "map"s key and value type don't match keyClass and valueClass
        if (!(keyValueContainer.get(name).getKey().equals(keyClass) && keyValueContainer.get(name).getValue().equals(valueClass))) {
            throw new ClassCastException("ERROR: Incorrect key class or value class.");
        }

        return map;
    }


    public <K, V> Map<K, V> createMap(String name, Class<K> keyClass, Class<V> valueClass) {

        //if client is not associated with a transaction
        transactionAssociation();

        //make a map of type (keyClass, valueClass) and store it in the map container - this is the map that must be transactionally aware, aka MyMap
        MyMap<K, V> newMap = new MyMap<>(name, keyClass, valueClass);
        mapContainer.put(name, newMap);

        //make a keyValue object and store it in key-value container
        keyValue newKv = new keyValue(keyClass, valueClass);
        keyValueContainer.put(name, newKv);

        return newMap;
    }


    private void transactionAssociation() {
        TxStatus status = null;
        try {
            status = TxMgrImpl.Instance.getTx().getStatus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (status == (TxStatus.NO_TRANSACTION)) {
            throw new ClientNotInTxException("ERROR: Client is not associated with a transaction.");
        }
    }


    private void serialize(MapObject obj) {
        try {
            if (fos == null) {
                fos = new FileOutputStream("shadowLog.txt");
                oos = new ObjectOutputStream(fos);
            }
            oos.writeObject(obj);
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void commitStreamCloser() {
        try {
            oos.close();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Object deserialize(Object key, MyMap thisMap) {
        Object returnValue = null;

        try {
            FileInputStream fis = new FileInputStream("shadowLog.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            boolean read = true;
            while (read) {
                try {
                    MapObject obj = null;
                    obj = (MapObject) ois.readObject();

                    if (obj.getKey().equals(key) && obj.getMethodName().equals("put") && obj.getMapName().equals(thisMap.getName())) {
                        returnValue = obj.getValue();
                    }
                    if (obj.getKey().equals(key) && obj.getMethodName().equals("get") && obj.getMapName().equals(thisMap.getName())) {
                        returnValue = obj.getValue();
                    }
                    if (obj.getKey().equals(key) && obj.getMethodName().equals("remove") && obj.getMapName().equals(thisMap.getName())) {
                        returnValue = null;
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

        return returnValue;
    }


    public Object removeDeserialize(Object key, MyMap thisMap) {
        Object returnValue = null;
        try {
            FileInputStream fis = new FileInputStream("shadowLog.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            boolean read = true;
            while (read) {
                try {
                    MapObject obj = null;
                    obj = (MapObject) ois.readObject();

                    if (obj.getKey().equals(key) && obj.getMethodName().equals("put") && obj.getMapName().equals(thisMap.getName())) {
                        returnValue = obj.getValue();
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

        return returnValue;
    }


    @Override
    public void setTxTimeoutInMillis(int ms) {
        this.TxTimeoutInMillis = ms;
    }


    @Override
    public int getTxTimeoutInMillis() {
        return this.TxTimeoutInMillis;
    }

}
