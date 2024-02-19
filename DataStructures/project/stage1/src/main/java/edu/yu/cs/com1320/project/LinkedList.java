package edu.yu.cs.com1320.project;

class LinkedList {
    Node head;
    Node tail;

    public LinkedList() {
        this.head = null;
        this.tail = null;
    }

    public void insert(Node node) {
        if (this.head == null) {
            this.head = node;
        }
        else {
            this.tail.next = node;
        }

        this.tail = node;
    }
}
