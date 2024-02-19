package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.BTree;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {                               //i think
    private static final int MAX = 4;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private DocumentIOImpl docIO;
    private List<URI> diskTree;

    private static final class Node {
        private int entryCount; // number of entries
        private Entry[] entries = new Entry[BTreeImpl.MAX]; // the array of children                                    //Map.Entry??
        private Node next;
        private Node previous;

        // create a node with k entries
        private Node(int k) {
            this.entryCount = k;
        }

        private void setNext(Node next) {
            this.next = next;
        }

        private Node getNext() {
            return this.next;
        }

        private void setPrevious(Node previous) {
            this.previous = previous;
        }

        private Node getPrevious() {
            return this.previous;
        }

        private Entry[] getEntries() {
            return Arrays.copyOf(this.entries, this.entryCount);
        }
    }

    public static class Entry {
        private Comparable key;
        private Object val;
        private Node child;

        public Entry(Comparable key, Object val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }

        public Object getValue() {
            return this.val;
        }

        public Comparable getKey() {
            return this.key;
        }
    }

//    public BTreeImpl() {
//        this.root = new Node(0);
//        this.leftMostExternalNode = this.root;
//        this.docIO = new DocumentIOImpl();
//        this.diskTree = new LinkedList<>();
//    }

    public BTreeImpl(File baseDir) {
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
        this.docIO = new DocumentIOImpl(baseDir);
        this.diskTree = new LinkedList<>();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public int size() {
        //returns number of key-value pairs in the symbol table
        return this.n;
    }

    public int height() {
        return this.height;
    }

    public ArrayList<Entry> getOrderedEntries() {
        Node current = this.leftMostExternalNode;
        ArrayList<Entry> entries = new ArrayList<>();
        while(current != null) {
            for(Entry e : current.getEntries()) {
                if(e.val != null) {
                    entries.add(e);
                }
            }
            current = current.getNext();
        }
        return entries;
    }

    public Entry getMinEntry() {
        Node current = this.leftMostExternalNode;
        while(current != null) {
            for(Entry e : current.getEntries()) {
                if(e.val != null) {
                    return e;
                }
            }
        }
        return null;
    }

    public Entry getMaxEntry() {
        ArrayList<Entry> entries = this.getOrderedEntries();
        return entries.get(entries.size()-1);
    }

    public Value get(Key key) {
        //this has to deal with the notion that if its not in memory aka has been serialized, then it has to be brought back into memory aka deserialized
        if (key == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        //if this key is not in the diskTree, then no need to deal with de/serialization
        if (!(diskTree.contains(key))) {
            Entry entry = this.get(this.root, key, this.height);
            if (entry != null) {
                return (Value) entry.val;
            }
            return null;
        }
        //otherwise it is in the diskTree, so it has to be deserialized in order to return the doc
        else {
            this.n++;
            diskTree.remove(key);
            return (Value) docIO.deserialize((URI) key);
        }
    }

    private Entry get(Node currentNode, Key key, int height) {
        Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0) {
            for (int j = 0; j < currentNode.entryCount; j++) {
                if (isEqual(key, entries[j].key)) {
                    //found desired key. Return its value
                    //if its value is null, that means it was "deleted", so return null
                    if (entries[j].val == null) {
                        return null;
                    }
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else {
            for (int j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)) {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            //didn't find the key
            return null;
        }
    }

    public Value put(Key key, Value val) {
        if (key == null) {
            throw new IllegalArgumentException("argument key to put() is null");
        }

        //if the key already exists in the b-tree, simply replace the value
        Entry alreadyThere = this.get(this.root, key, this.height);
        if (alreadyThere != null) {
            if (diskTree.contains(alreadyThere.key)) {
                Value oldValue = (Value) docIO.deserialize((URI) alreadyThere.key);
                return oldValue;
            }
            else {
                //save old value for sake of return
                Value oldValue = (Value) alreadyThere.val;
                //replace that old value with new added value
                alreadyThere.val = val;
                return oldValue;                                                                                            //im assuming
            }
        }

        Node newNode = this.put(this.root, key, val, this.height);
        this.n++;
        if (newNode == null) {
            return null;                                                                                                //maybe??
        }

        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return null;                                                                                                    //dont quote me on that
    }

    private Node put(Node currentNode, Key key, Value val, int height) {
        //@return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node

        int j;
        Entry newEntry = new Entry(key, val, null);

        //external node
        if (height == 0) {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        }

        // internal node
        else {
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX) {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else {
            //will have to create new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    public void delete(Key key) {
        put(key, null);
        n--;
    }

    private Node split(Node currentNode, int height) {
        Node newNode = new Node(BTreeImpl.MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < BTreeImpl.MAX / 2; j++) {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        //external node
        if (height == 0) {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    public void moveToDisk(Key k) throws Exception {
        //instance of DocIO
        //get of this key to get the value aka doc
        //now you can serialize this doc
        docIO.serialize((DocumentImpl)get(k));
        diskTree.add((URI) k);
        this.n--;
        //i think needs to set the value now to a location record on disk
    }
}
