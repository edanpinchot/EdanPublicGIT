package edu.yu.introtoalgs;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ParityFJ extends RecursiveTask<Boolean> {
    private int[] arr;
    private int start;
    private int end;
    int threshold = 1;

    public ParityFJ(int[] arr, int start, int end) {
        this.arr = arr;
        this.start = start;
        this.end = end;
    }

    //computes whether or not the given array has an even number of evens
    public static boolean parity(int[] arr) {
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        return pool.invoke(new ParityFJ(arr, 0, arr.length));
    }

    protected Boolean compute() {
        if ((end - start) <= threshold) {
            int count = 0;
            for (int i = 0; i < arr.length; i++) {
                if ((arr[i] % 2) == 0) {
                    count++;
                }
            }

            if ((count % 2) == 0) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            int middle = (end + start) / 2;

            ParityFJ subtask1 = new ParityFJ(arr, start, middle);
            ParityFJ subtask2 = new ParityFJ(arr, middle, end);

            //invokeAll(subtask1, subtask2);
            subtask1.fork();
            subtask2.fork();

            return (subtask1.join() && subtask2.join());
        }
    }

    public static void main(String[] args) {
        int[] array = {1, 7, 4, 3, 6};
        int[] array2 = {6, 5, 4, 3, 2, 1};

        System.out.println(parity(array));
        System.out.println(parity(array2));
    }
}
