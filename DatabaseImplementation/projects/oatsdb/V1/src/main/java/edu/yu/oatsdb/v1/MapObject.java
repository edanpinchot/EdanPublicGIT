package edu.yu.oatsdb.v1;

public class MapObject<K, V> implements java.io.Serializable {
    private String methodName;
    private Object key;
    private Object value;
    private String mapName;

    public MapObject(String methodName, Object key, Object value, String mapName) {
        this.methodName = methodName;
        this.key = key;
        this.value = value;
        this.mapName = mapName;
    }

    public MapObject(String methodName, Object key) {
        this.methodName = methodName;
        this.key = key;
        this.value = null;
    }

    public String getMethodName() {
        return methodName;
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
