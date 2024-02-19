package edu.yu.oatsdb.v0;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.yu.oatsdb.base.*;

public enum DBMSImpl implements DBMS {
    Instance;
    //need a Map to serve as the container of containers - associates Map Name with arbitrary generic (not raw) Map instances
    private Map<String, MyMap> mapContainer = new HashMap<>();
    private Map<String, keyValue> keyValueContainer = new HashMap<>();
    private TxMgrImpl TxMgr = TxMgrImpl.Instance;

    //Override map methods, throwing errors if they are called without transaction association
    public class MyMap<K, V> extends HashMap {
        @Override
        public Object put(Object key, Object value) {
            transactionAssociation();
            return super.put(key, value);
        }

        @Override
        public Object get(Object key) {
            transactionAssociation();
            return super.get(key);
        }

        @Override
        public Object remove(Object key) {
            transactionAssociation();
            return super.remove(key);
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
        MyMap<K, V> newMap = new MyMap<>();
        mapContainer.put(name, newMap);

        //make a keyValue object and store it in key-value container
        keyValue newKv = new keyValue(keyClass, valueClass);
        keyValueContainer.put(name, newKv);

        return newMap;
    }


    private void transactionAssociation() {
        TxStatus status = null;
        try {
            status = TxMgr.getTx().getStatus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (status == (TxStatus.NO_TRANSACTION)) {
            throw new ClientNotInTxException("ERROR: Client is not associated with a transaction.");
        }
    }


}
