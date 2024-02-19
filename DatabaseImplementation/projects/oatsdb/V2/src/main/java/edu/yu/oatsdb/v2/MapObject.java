package edu.yu.oatsdb.v2;

public class MapObject<K, V> implements java.io.Serializable {
    private String mapName;
    private Object key;
    private Object value;

    public MapObject(String mapName, Object key, Object value) {
        this.mapName = mapName;
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getMapName() {
        return mapName;
    }
}
