package edu.yu.oatsdb.v2;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.yu.oatsdb.base.*;


public enum DBMSImpl implements ConfigurablePersistentDBMS  {
    Instance;
    //need a Map to serve as the container of containers - associates Map Name with arbitrary generic (not raw) Map instances
    private Map<String, MyMap> mapContainer = new HashMap<>();
    private Map<String, keyValue> keyValueContainer = new HashMap<>();
    private ArrayList<MapAndShadow> shadowsMapped = new ArrayList<>();
    private FileOutputStream fos = null;
    private ObjectOutputStream oos;
    private FileOutputStream fos2 = null;
    private ObjectOutputStream oos2;
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

        public Class<K> getKeyClass() {
            return keyClass;
        }

        public Class<V> getValueClass() {
            return valueClass;
        }

        public Map<K, Lock> getLockedEntries() {
            return lockedEntries;
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
            if (!(key instanceof Serializable)) {
                throw new IllegalArgumentException("Key inserted is not serializable");
            }

            //lock this map entry
            lock((K) key);

            return super.put(key, value);
        }

        @Override
        public Object get(Object key) {
            transactionAssociation();

            //no serialization for a "get" - just deserialize until the "put" for this map is found, and return the value
            //on second thought - need to serialize "get" so that the lock on this map will be undone
//            Object value = deserialize(key, this);
//            MapObject obj = new MapObject("get", key, value, this.name);
//            serialize(obj);

            //lock this map entry
            lock((K) key);

//            return value;
            return super.get(key);
        }

        @Override
        public Object remove(Object key) {
            transactionAssociation();

            //serialize
//            MapObject obj = new MapObject("remove", key, null, this.name);
//            serialize(obj);
//
//            return removeDeserialize(key, this);

            return super.remove(key);
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


    public ArrayList<MapAndShadow> getShadowsMapped() {
        return shadowsMapped;
    }


    public <K, V> Map<K, V> getMap(final String name, Class<K> keyClass, Class<V> valueClass) {
        Map<K, V> shadowMap = null;

        //if client is not associated with a transaction
        transactionAssociation();

        //first pull the actual map with this "name" from our container
        Map<K, V> actualMap = mapContainer.get(name);
        //then get this actual map's shadow map
        for(MapAndShadow ms : shadowsMapped) {
            if (ms.getActualMap() == actualMap) {
                shadowMap = ms.getShadowMap();
            }
        }

        //if no map is associated with the specified name
        if (actualMap == null) {
            throw new NoSuchElementException("ERROR: No map is associated with the specified name.");
        }

        //throw an error if "map"s key and value type don't match keyClass and valueClass
        if (!(keyValueContainer.get(name).getKey().equals(keyClass) && keyValueContainer.get(name).getValue().equals(valueClass))) {
            throw new ClassCastException("ERROR: Incorrect key class or value class.");
        }

        return shadowMap;
    }


    public <K, V> Map<K, V> createMap(String name, Class<K> keyClass, Class<V> valueClass) {
        if(mapContainer.containsKey(name)) {
            throw new IllegalArgumentException("ERROR: Map already exists with this name");
        }

        //if client is not associated with a transaction
        transactionAssociation();

        //make a map of type (keyClass, valueClass) and store it in the map container - this is the map that must be transactionally aware, aka MyMap
        MyMap<K, V> actualMap = new MyMap<>(name, keyClass, valueClass);
        MyMap<K, V> shadowMap = new MyMap<>(name, keyClass, valueClass);
        mapContainer.put(name, actualMap);

        MapAndShadow ms = new MapAndShadow(actualMap, shadowMap);
        shadowsMapped.add(ms);

        //make a keyValue object and store it in key-value container
        keyValue newKv = new keyValue(keyClass, valueClass);
        keyValueContainer.put(name, newKv);

        //create 3 files on disk to be stored for this map in directory, as well as file to store names of all the maps
        String path = System.getProperty("user.home");
        try {
            File theDir = new File(path + "/MapFiles");
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            fos = new FileOutputStream(path + "/MapFiles/" + name + "_0.txt");
            fos = new FileOutputStream(path + "/MapFiles/" + name + "_1.txt");
            fos = new FileOutputStream(path + "/MapFiles/" + name + "_pointer.txt");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(0);

            if (fos2 == null) {
                fos2 = new FileOutputStream(path + "/MapFiles/mapNames.txt");
                oos2 = new ObjectOutputStream(fos2);
            }
            oos2.writeObject(name);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return shadowMap;
    }


    public void createAnew(String mapName, MyMap map) {
        mapContainer.put(mapName, map);
        keyValue newKv = new keyValue(map.getKeyClass(), map.getValueClass());
        keyValueContainer.put(mapName, newKv);
        MyMap map2 = new MyMap(mapName, map.getKeyClass(), map.getValueClass());
        map2.putAll(map);

        MapAndShadow ms = new MapAndShadow(map, map2);
        shadowsMapped.add(ms);
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


    public void commitStreamCloser() {
        try {
            if ((oos != null) && (fos != null)) {
                oos.close();
                fos.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setTxTimeoutInMillis(int ms) {
        this.TxTimeoutInMillis = ms;
    }

    @Override
    public int getTxTimeoutInMillis() {
        return this.TxTimeoutInMillis;
    }

    @Override
    public double getDiskUsageInMB() {
        double totalMB = 0;

        String path = System.getProperty("user.home");
        File file = new File(path + "/MapFiles/");
        File[] files = file.listFiles();

        for (int k = 0; k < files.length; k++) {
            double kb = (files[k].length() / 1024.0);
            double mb = (kb / 1024.0);
            totalMB += mb;
        }

        return totalMB;
    }

    @Override
    public void clear() {
        String path = System.getProperty("user.home");
        File file = new File(path + "/MapFiles/");
        deleteDirectory(file);

        mapContainer.clear();
        keyValueContainer.clear();
        shadowsMapped.clear();
    }

    public void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}

