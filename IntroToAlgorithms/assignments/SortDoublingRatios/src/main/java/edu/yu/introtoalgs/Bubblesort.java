package edu.yu.introtoalgs;

public class Bubblesort implements Sorter {

    /** No-argument constructor: should not be instantiated outside package
     */
    Bubblesort() {
    }

    public void sortIt(final Comparable a[]) {

        int n = a.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (a[j].compareTo(a[j + 1]) > 0) {
                    Comparable temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }

    }


}