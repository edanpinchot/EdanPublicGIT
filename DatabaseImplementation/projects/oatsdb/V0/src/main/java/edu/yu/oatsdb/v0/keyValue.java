package edu.yu.oatsdb.v0;

public class keyValue<K, V> {
    private Class<K> key;
    private Class<V> value;

    keyValue(Class<K> key, Class<V> value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(Class<K> key) {
        this.key = key;
    }

    public Class<K> getKey() {
        return key;
    }

    public void setValue(Class<V> value) {
        this.value = value;
    }

    public Class<V> getValue() {
        return value;
    }

}
