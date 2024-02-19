package edu.yu.cs.com1320.project.Impl;

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

    protected void displayList() {
        while (this.head != null) {
            System.out.println(this.head.k + " " + this.head.v);
            this.head = this.head.next;
        }
    }
}
