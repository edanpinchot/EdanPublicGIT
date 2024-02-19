package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.net.URI;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable> extends MinHeap<E> {

    public MinHeapImpl(E[] elements) {
        this.elements = elements;                       //this.elements = (E[]) new DocumentImpl[2]; ?
        this.count = 0;
        this.elementsToArrayIndex = new HashMap<>();
        this.docRecords = new HashMap<>();
    }

    public void reHeapify(E element) {
        //first add this element to the map if not already there
        if ((elementsToArrayIndex.isEmpty()) || !(elementsToArrayIndex.containsKey(element))) {
            elementsToArrayIndex.put(element, count);
        }
        //set 'index' to where we are right now
        int index = getArrayIndex(element);

        upHeap(index);
        downHeap(index);
        //now take care of re-mapping elements to their present location, after having reheaped
        for (int i = 1; i < elements.length; i++) {
            elementsToArrayIndex.put(elements[i], i);
        }
    }

    protected int getArrayIndex(E element) {
        return elementsToArrayIndex.get(element);
    }

    protected void doubleArraySize() {
        E[] temp = (E[]) new URI[2 * elements.length];
        for (int i = 0; i < elements.length; i++) {
            temp[i] = elements[i];
        }
        elements = temp;
    }

    protected void docIntoHeap(URI uri, DocumentImpl doc) {
        this.docRecords.put(uri, doc);
    }

    protected void docOutOfHeap(URI uri) {
        this.docRecords.remove(uri);
    }

    @Override   //to account for keeping track of array indexes
    public E removeMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC

        //remove min from the map and re-map everything to where it is presently
        elementsToArrayIndex.remove(min);
        for (int i = 1; i < elements.length; i++) {
            elementsToArrayIndex.put(elements[i], i);
        }
        return min;
    }
}