package edu.yu.cs.com1320.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

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

    protected void arrayDouble() {                                              //if all supposed to be done in this class, just make this method take an array as parameter and return the new doubled array, and then in the "put" method, at the end make this.table = arrayDouble(theOldTable)
        System.out.println("how much is in it: " + this.size);
        System.out.println("what's da capacity: " + this.table.length);
        if (( (double)this.size / (double)this.table.length) >= 0.7) {
            //create the double-sized array
            LinkedList[] doubledTable = new LinkedList[2 * (this.table.length)];
            int index2;
            //fill the new array with empty lists to avoid Null pointer errors
            for (int j = 0; j < doubledTable.length; j++) {
                doubledTable[j] = new LinkedList();
            }
            //iterate through previous table, get the non-null elements, rehash them and then place them into the newly hashed index in the new doubled array
            for (int i = 0; i < this.table.length; i++) {
                if (table[i].head != null) {
                    index2 = (Math.abs(table[i].head.k.hashCode())) % doubledTable.length;
                    doubledTable[index2] = this.table[i];
                }
            }
            //swap out old table for new, doubled table
            this.table = doubledTable;
        }
    }

    public Value put(Key k, Value v) {
        if ((k == null) || (k.toString().length() == 0)) {
            throw new RuntimeException("ERROR: Invalid key.");
        }

        Node node = new Node(k, v);
        int index = hash(k);
        LinkedList list = table[index];
        Value previousValue = null;

        if (list.head == null) {
            list.insert(node);
            size++;
            return null;
        }
        else {
            while (list.head != null) {
                if (list.head.k.equals(k)) {
                    previousValue = (Value) list.head.v;
                    list.head.setValue(v);
                }
                list.head = list.head.next;
                size++;
            }
        }
        list.insert(node);

        return previousValue;
    }

    public Value get(Key k) {
        if ((k == null) || (k.toString().length() == 0)) {
            throw new RuntimeException("ERROR: Invalid key.");
        }

        int index = hash(k);
        LinkedList list = table[index];

        while (list.head != null) {
            if (list.head.k.equals(k)) {
                return (Value) list.head.v;
            }
            list.head = list.head.next;
        }

        return null;
    }

    public void remove(Key k) {
        int slot = hash(k);

        //first option: if there is no List there at all, then exit
        if (table[slot] == null) {
            return;
        }

        //second option: if the node we want to delete is at the head of the list, then delete by pointing the head to the second node instead
        if (table[slot].head.k.equals(k)) {
            table[slot].head = table[slot].head.next;
            size--;
            return;
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
    }

    public static void main(String[] args) {
        StackImpl myStack = new StackImpl();
        myStack.push("adir");
        myStack.displayEntireStack();
        System.out.println();

        myStack.push("arianne");
        myStack.displayEntireStack();
        System.out.println();

        myStack.push("rami");
        myStack.displayEntireStack();
        System.out.println();

        myStack.push("edan");
        myStack.displayEntireStack();
        System.out.println();

        myStack.push("lior");
        myStack.displayEntireStack();
        System.out.println();


    }

//    public static void main(String[] args) {
//        DocumentStoreImpl store = new DocumentStoreImpl();
//        String string = "gyyggygygyygygyjje";
//        byte[] compressedString = store.gzipCompress(string);
//        for (int i = 0; i > compressedString.length; i++) {
//            System.out.println(compressedString[i]);
//        }
//        System.out.println(compressedString);
//        String uncompressedArray = store.gzipUncompress(compressedString);
//        System.out.println(uncompressedArray);
//        System.out.println();
//
//        DocumentStoreImpl docStore = new DocumentStoreImpl();
//
//        String s = "datastructuresblahbblahsssssssssssssssllslslslslslslslslslsl";
//        InputStream stream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
//        URI uri = URI.create(s);
//        System.out.println(docStore.putDocument(stream, uri, DocumentStore.CompressionFormat.ZIP));
//        System.out.println(docStore.getDocument(uri));
//        System.out.println();
//
//        String p = "edanpinchotisamrfcrfcrfcrfcrfcrfcr";
//        InputStream in = new ByteArrayInputStream(p.getBytes(StandardCharsets.UTF_8));
//        URI u = URI.create(p);
//        System.out.println(docStore.putDocument(in, u));
//        System.out.println(docStore.getDocument(u));
//        System.out.println();
//
//        String k = "hihihihihihi";
//        InputStream inny = new ByteArrayInputStream(k.getBytes(StandardCharsets.UTF_8));
//        URI you = URI.create(k);
//        System.out.println(docStore.putDocument(inny, you, DocumentStore.CompressionFormat.ZIP));
//        System.out.println(docStore.getDocument(you));
//        System.out.println();
//
//        String j = "ayoayoayo";
//        InputStream inyo = new ByteArrayInputStream(j.getBytes(StandardCharsets.UTF_8));
//        URI ue = URI.create(j);
//        System.out.println(docStore.putDocument(inyo, ue, DocumentStore.CompressionFormat.BZIP2));
//        System.out.println(docStore.getDocument(ue));
//        System.out.println();
//
//        String m = "onetwothreefourfivesixseveneightnine";
//        InputStream rty = new ByteArrayInputStream(m.getBytes(StandardCharsets.UTF_8));
//        URI qwe = URI.create(m);
//        System.out.println(docStore.putDocument(rty, qwe, DocumentStore.CompressionFormat.BZIP2));
//        System.out.println(docStore.getDocument(qwe));
//        System.out.println();

//        System.out.println(docStore.putDocument(in, u, DocumentStore.CompressionFormat.BZIP2));
//        docStore.deleteDocument(uri);
//        System.out.println(docStore.getDocument(uri));
//        System.out.println(docStore.deleteDocument(uri));
//        System.out.println(docStore.getDocument(uri));
//
//    }

//    public static void main(String[] args) {
//        Node node1 = new Node("edan", 20);
//        Node node2 = new Node("yehoshua", 21);
//        Node node3 = new Node("eitan", 19);
//        LinkedList friendsAges = new LinkedList();
//
//        friendsAges.insert(node1);
//        friendsAges.insert(node2);
//        friendsAges.insert(node3);
//
//        while (friendsAges.head != null) {
//            System.out.println(friendsAges.head.k);
//            friendsAges.head = friendsAges.head.next;
//        }
//        System.out.println();
//
//        HashTableImpl hashTable = new HashTableImpl(64);
//        System.out.println(hashTable.put("edan", "8477081225"));
//        System.out.println(hashTable.put("rami", "8472751193"));
//        //hashTable.remove("rami");
//        System.out.println(hashTable.put("edan", "8473724399"));
//        System.out.println();
//        System.out.println(hashTable.get("edan"));
//        System.out.println(hashTable.get("rami"));
//        System.out.println(hashTable.get("fakeKey"));
//    }

}