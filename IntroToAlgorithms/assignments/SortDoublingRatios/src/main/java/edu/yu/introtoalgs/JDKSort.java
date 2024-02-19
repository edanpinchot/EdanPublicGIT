package edu.yu.introtoalgs;

import java.util.Arrays;

public class JDKSort implements Sorter {

    /** No-argument constructor: should not be instantiated outside package
     */
    JDKSort() {
    }

    public void sortIt(final Comparable a[]) {
        Arrays.sort(a);
    }
}