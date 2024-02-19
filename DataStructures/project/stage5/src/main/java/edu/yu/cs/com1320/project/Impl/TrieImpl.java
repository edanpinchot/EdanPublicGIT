package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Trie;

import java.net.URI;
import java.util.*;
import java.util.LinkedList;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 256; // extended ASCII
    private Node root; // root of trie
    private Map<URI, DocumentImpl> docsInTrie;

    public static class Node<Value> {
        protected java.util.LinkedList<Value> valueList;
        protected Node[] links = new Node[TrieImpl.alphabetSize];
    }

    public TrieImpl() {
        this.docsInTrie = new HashMap<>();
    }

    public List<Value> getAllSorted(String key) {                   //uses java.util.Comparator<Document>
        class sortByNumber implements Comparator<DocumentImpl> {
            public int compare(DocumentImpl a, DocumentImpl b) {
                return b.wordCount(key) - a.wordCount(key);
            }
        }

        Node endOfWord = get(this.root, key);
        //list of the URIs with this word
        List<URI> wheresThisWordURIs = (List<URI>) endOfWord.valueList;

        //convert that into a list of documents so that we can use our sortByNumber method
        List<DocumentImpl> wheresThisWordDocs = new LinkedList<>();
        for (URI uri : wheresThisWordURIs) {
            DocumentImpl doc = docsInTrie.get(uri);
            wheresThisWordDocs.add(doc);
        }
        Collections.sort(wheresThisWordDocs, new sortByNumber());

        //now take sorted list of documents and convert it back to URIs so we can return
        List<URI> sortedURIS = new LinkedList<>();
        for (DocumentImpl doc : wheresThisWordDocs) {
            sortedURIS.add(doc.getKey());
        }

        return (List<Value>) sortedURIS;
    }

    public void put(String key, Value val) {
        //deleteAll the value from this key
        key = key.toLowerCase();                                    //make it all lowercase to be safe
        if (val == null) {
            this.deleteAll(key);
        } else {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node put(Node x, String key, Value val, int d) {
        //create a new node
        if (x == null) {
            x = new Node();
        }
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length()) {
            //if the node we've landed on has no list at all, give it a new empty list
            if (x.valueList == null) {
                x.valueList = new LinkedList();
            }
            //add the value (in our case it will be a document) to the list of values at that node
            if (!x.valueList.contains(val)) {
                x.valueList.add(val);
            }
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    public void deleteAll(String key) {
        key = key.toLowerCase();
        this.root = deleteAll(this.root, key, 0);
    }

    private Node deleteAll(Node x, String key, int d) {
        if (x == null) {
            return null;
        }
        //we're at the node to del - set the val to null
        if (d == key.length()) {
            x.valueList = null;
        }
        //continue down the trie to the target node
        else {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (x.valueList != null) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c < alphabetSize; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    public void delete(String key, Value val) {
        key = key.toLowerCase();
        this.root = delete(this.root, key, val, 0);
    }

    private Node delete(Node x, String key, Value val, int d) {
        if (x == null) {
            return null;
        }
        //we're at the node to del - instead of setting whole list to null, just .remove() the given value
        if (d == key.length()) {
            //if 'delete' was called on the only element in the value list, then we want to deleteAll
            if (x.valueList.size() == 1) {
                deleteAll(key);
            }
            //if there are elements other than the one specified in the list, then just remove this specific one
            else {
                x.valueList.remove(val);
            }
        }
        //continue down the trie to the target node
        else {
            char c = key.charAt(d);
            x.links[c] = this.delete(x.links[c], key, val, d + 1);
        }
        //this node has a val – do nothing, return the node
        if (x.valueList != null) {
            return x;
        }
        //remove subtrie rooted at x if it is completely empty
        for (int c = 0; c < alphabetSize; c++) {
            if (x.links[c] != null) {
                return x; //not empty
            }
        }
        //empty - set this link to null in the parent
        return null;
    }

    private Node get(Node x, String key) {
        key = key.toLowerCase();
        //get me to the last node aka last letter of this word
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            x = x.links[c];
        }
        return x;
    }

    protected void docIntoTrie(URI uri, DocumentImpl doc) {
        this.docsInTrie.put(uri, doc);
    }

    protected void docOutOfTrie(URI uri) {
        this.docsInTrie.remove(uri);
    }
}