package edu.yu.introtoalgs;

import java.util.LinkedList;

public class BinarySearchST<Key extends Comparable<Key>, Value> {
    private Key[] keys;
    private Value[] vals;
    private int n;

    public BinarySearchST() {
        keys = (Key[]) new Comparable[2];
        vals = (Value[]) new Object[2];
    }

    public BinarySearchST(Key[] initialKeys , Value[] initialValues) {
        //defend against insertion of size-zero arrays
        if (initialKeys.length == 0) {
            keys = (Key[]) new Comparable[2];
            vals = (Value[]) new Object[2];
        }
        else {
            keys = (Key[]) new Comparable[initialKeys.length];
            vals = (Value[]) new Object[initialValues.length];

            for (int p = 0; p < initialKeys.length; p++) {
                this.put(initialKeys[p], initialValues[p]);
            }
        }
    }

    public int size() {
        return n;
    }

    public void resize(int size) {
        Key[] keysNew = (Key[]) new Comparable[size];
        Value[] valsNew = (Value[]) new Object[size];

        for (int p = 0; p < keys.length; p++) {
            keysNew[p] = keys[p];
            valsNew[p] = vals[p];
        }

        keys = keysNew;
        vals = valsNew;
    }

    public int rank(Key key) {
        int lo = 0;
        int hi = (n-1);
        while (lo <= hi) {
            int mid = lo + (hi -lo) / 2;
            int cmp = key.compareTo(keys[mid]);
            if (cmp < 0) {
                hi = (mid - 1);
            }
            else if (cmp > 0) {
                lo = (mid + 1);
            }
            else {
                return mid;
            }
        }
        return lo;
    }

    public Value get(Key key) {
        if (n == 0) {
            return null;
        }
        int i = rank(key);
        if ((i < n) && (keys[i].compareTo(key) == 0)) {
            return vals[i];
        }
        else {
            return null;
        }
    }

    public void put(Key key, Value value) {
        if (key == null) {
            throw new IllegalArgumentException("Cannot input null key");
        }

        if (value == null) {
            this.delete(key);
        }

        else {
            int i = rank(key);
            //if duplicating a key, just replace the key's value
            if ((i < n) && (keys[i].compareTo(key) == 0)) {
                vals[i] = value;
                return;
            }

            //resize if we're about to max out on the next put (resizing one earlier so we don't have an out of bounds index when shifiting items after a delete)
            if (keys.length == (n + 1)) {
                resize(2 * keys.length);
            }

            for (int j = n; j > i; j--) {
                keys[j] = keys[j - 1];
                vals[j] = vals[j - 1];
            }
            keys[i] = key;
            vals[i] = value;
            n++;
        }
    }

    public Key min() {
        return keys[0];
    }

    public Key max() {
        return keys[n-1];
    }

    public Key select(int k) {
        return keys[k];
    }

    public Key ceiling(Key key) {
        int i = rank(key);

        if (i == n) {
            return null;
        }

        return keys[i];
    }

    public Key floor(Key key) {
        int i = rank(key);

        if ((i < n) && (keys[i].compareTo(key) == 0)) {
            return keys[i];
        }

        if (i == 0) {
            return null;
        }

        return (keys[i-1]);
    }

    public void delete(Key key) {
        int i = rank(key);

        if ((i < n) && (keys[i].compareTo(key) == 0)) {
            //set deleted key and value to null
            vals[i] = null;
            keys[i] = null;
            n--;

            //now slide any existent keys above the deleted key down a slot
            for (int p = i; p <= n; p++) {
                    keys[p] = keys[p + 1];
                    vals[p] = vals[p + 1];
            }
        }
    }

    public Iterable<Key> keys() {
        LinkedList<Key> list = new LinkedList<Key>();
        //add keys to list as long as there are still keys left and they are not null
        for (int p = 0; ((p < keys.length) && (keys[p] != null)); p++) {
                list.add(keys[p]);
        }

        return list;
    }

    public static void main(String[] args) {
        Integer[] initialKeys = {16, 11};
        String[] initialValues = {"Danni", "Jonah"};
        BinarySearchST bsst = new BinarySearchST(initialKeys, initialValues);
//        BinarySearchST bsst = new BinarySearchST();

        bsst.put(21, "Edan");
        bsst.put(25, "Arianne");
        bsst.put(17, "Lior");
        bsst.put(27, "Adir");
        bsst.put(23, "Rami");
        bsst.put(52, "Laurie");
        bsst.put(2, "Sophia");
        bsst.put(0, "Ari Shalom");
        bsst.put(1, "Miri");
//        bsst.delete(23);
        bsst.put(0, null);
//        bsst.put(0, null);
        System.out.println((bsst.floor(0)));

        Iterable<Object> list = bsst.keys();
        System.out.println(list);
    }

}
