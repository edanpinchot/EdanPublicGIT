package edu.yu.cs.com1320.project.Impl;

class Node<Key, Value> {
    Key k;
    Value v;
    Node next;

    public Node(Key k, Value v) {
        this.k = k;
        this.v = v;
        this.next = null;
    }

    public void setValue(Value v) {
        this.v = v;
    }

    private Value getValue() {
        return v;
    }
}
