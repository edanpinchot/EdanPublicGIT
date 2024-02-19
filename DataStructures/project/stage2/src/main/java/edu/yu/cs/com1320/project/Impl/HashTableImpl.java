package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.DocumentStore;
import edu.yu.cs.com1320.project.HashTable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    private LinkedList[] table;
    private int size;

    public HashTableImpl(int initialSize) {
        table = new LinkedList[initialSize];
        size = 0;

        for (int i = 0; i < initialSize; i++) {
            table[i] = new LinkedList();
        }
    }

    protected int hash(Key k) {
        return (Math.abs(k.hashCode())) % table.length;
    }

    protected void arrayDouble() {
        if (( (double)this.size / (double)this.table.length) >= 0.7) {
            //make an alternate table, pour everything from "table" into it to make it identical
            LinkedList[] altTable = new LinkedList[this.table.length];
            for (int k = 0; k < table.length; k++) {
                altTable[k] = table[k];
            }
            //now that we've "saved" table into altTable, reset "table" to being empty and double the size
            this.table = new LinkedList[(2 * table.length)];
            for (int p = 0; p < table.length; p++) {
                table[p] = new LinkedList();
            }
            //now iterate through altTable, "putting" every element into now-empty "table", which will now re-hash every single element
            for (int j = 0; j < altTable.length; j++) {
                if (altTable[j].head != null) {
                    doublePut((Key) altTable[j].head.k, (Value) altTable[j].head.v);
                    Node current = altTable[j].head.next;
                    while (current != null) {
                        doublePut((Key) current.k, (Value) current.v);
                        current = current.next;
                    }
                }
            }
        }
    }

    public Value put(Key k, Value v) {
        arrayDouble();

        int slot = hash(k);
//        System.out.println("Hashing to: " + hash(k));
        Node node = new Node(k, v);
        Value previousValue;

        if (table[slot].head == null) {
            table[slot].insert(node);
            size++;
            return null;
        }

        if (table[slot].head.k.equals(k)) {
            if (v == null) {
                remove(k);
            }
            previousValue = (Value) table[slot].head.v;
            table[slot].head.setValue(v);
            return previousValue;
        }

        Node previous = table[slot].head;
        Node current = table[slot].head.next;
        while ((current != null) && !(current.k.equals(k))) {
            previous = current;
            current = current.next;
        }
        if (current == null) {
            table[slot].insert(node);
            size++;
            return null;
        }
        if (current.k.equals(k)) {
            if (v == null) {
                remove(k);
            }
            previousValue = (Value) current.v;
            current.setValue(v);
            return previousValue;
        }
        return null;
    }

    protected Value doublePut(Key k, Value v) {
        int slot = hash(k);
//        System.out.println("Re-hashing to: " + hash(k));
        Node node = new Node(k, v);
        Value previousValue;

        if (table[slot].head == null) {
            table[slot].insert(node);
            return null;
        }

        if (table[slot].head.k.equals(k)) {
            if (v == null) {
                remove(k);
            }
            previousValue = (Value) table[slot].head.v;
            table[slot].head.setValue(v);
            return previousValue;
        }

        Node previous = table[slot].head;
        Node current = table[slot].head.next;
        while ((current != null) && !(current.k.equals(k))) {
            previous = current;
            current = current.next;
        }
        if (current == null) {
            table[slot].insert(node);
            return null;
        }
        if (current.k.equals(k)) {
            if (v == null) {
                remove(k);
            }
            previousValue = (Value) current.v;
            current.setValue(v);
            return previousValue;
        }
        return null;
    }

    public Value get(Key k) {
        int slot = hash(k);

        if (table[slot].head == null) {
            return null;
        }

        if (table[slot].head.k.equals(k)) {
            return (Value) table[slot].head.v;
        }

        Node previous = table[slot].head;
        Node current = table[slot].head.next;
        while ((current != null) && !(current.k.equals(k))) {
            previous = current;
            current = current.next;
        }
        if (current == null) {
            return null;
        }

        return (Value) current.v;
    }

    public boolean remove(Key k) {
        int slot = hash(k);

        //first option: if there is no List there at all, then exit
        if (table[slot] == null) {
            return false;
        }

        //second option: if the node we want to delete is at the head of the list, then delete by pointing the head to the second node instead
        if (table[slot].head.k.equals(k)) {
            table[slot].head = table[slot].head.next;
            size--;
            return true;
        }

        //third option: if the node we want to delete is not at the head, then traverse the list to locate it, and point its previous node to its next node
        Node previous = table[slot].head;
        Node current = table[slot].head.next;
        while ((current != null) && !(current.k.equals(k))) {
            previous = current;
            current = current.next;
        }
        if (current != null) {
            previous.next = current.next;
            size--;
        }
        return true;
    }

    public static void main(String[] args) {
//        DocumentStoreImpl store = new DocumentStoreImpl();
//        String string = "gyyggygygyygygyjje";
//        byte[] compressedString = store.sevenzCompress(string);
//        for (int i = 0; i > compressedString.length; i++) {
//            System.out.println(compressedString[i]);
//        }
//        System.out.println(compressedString);
//        String uncompressedArray = store.sevenzUncompress(compressedString);
//        System.out.println(uncompressedArray);
//        System.out.println();

        DocumentStoreImpl docStore = new DocumentStoreImpl();

        String s = "data data ";
        InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        s = s.replaceAll("[^A-Za-z]+", "").toLowerCase();
        URI uri = URI.create(s);
        docStore.putDocument(stream, uri, DocumentStore.CompressionFormat.ZIP);

        String p = "za za";
        InputStream in = new ByteArrayInputStream(p.getBytes(StandardCharsets.UTF_8));
        p = p.replaceAll("[^A-Za-z]+", "").toLowerCase();
        URI u = URI.create(p);
        docStore.putDocument(in, u);

        String k = "ed ed data";
        InputStream inny = new ByteArrayInputStream(k.getBytes(StandardCharsets.UTF_8));
        k = k.replaceAll("[^A-Za-z]+", "").toLowerCase();
        URI you = URI.create(k);
        docStore.putDocument(inny, you, DocumentStore.CompressionFormat.ZIP);

        String j = "k data ki data data data";
        InputStream inyo = new ByteArrayInputStream(j.getBytes(StandardCharsets.UTF_8));
        j = j.replaceAll("[^A-Za-z]+", "").toLowerCase();
        URI ue = URI.create(j);
        docStore.putDocument(inyo, ue, DocumentStore.CompressionFormat.BZIP2);

        docStore.deleteDocument(you);
        docStore.undo();

        List<String> wheresthisword = docStore.search("ed");
        for (String string : wheresthisword) {
            System.out.println(string);
        }

    }

}