package edu.yu.introtoalgs;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class ReplaceWithPowerModFJ extends RecursiveAction {
    private int[] arr;
    public int start;
    public int end;
    int threshold = 1;
    private int power;
    private int mod;

    public ReplaceWithPowerModFJ(int[] arr, int start, int end, int power, int mod) {
        this.arr = arr;
        this.start = start;
        this.end = end;
        this.power = power;
        this.mod = mod;
    }

    public static void doIt(final int[] array, final int power, final int mod) {
        ReplaceWithPowerModFJ rp = new ReplaceWithPowerModFJ(array, 0, array.length, 2, 5);
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        pool.invoke(rp);

    }

    protected void compute() {
        if ((end - start) <= threshold) {
            for (int i = start; i < end; i++) {
                arr[i] = (int) ((Math.pow(arr[i], power)) % mod);
            }        }
        else {
            int middle = (end + start) / 2;

            ReplaceWithPowerModFJ subtask1 = new ReplaceWithPowerModFJ(arr, start, middle, power, mod);
            ReplaceWithPowerModFJ subtask2 = new ReplaceWithPowerModFJ(arr, middle, end, power, mod);

            //invokeAll(subtask1, subtask2);
            subtask1.fork();
            subtask2.compute();

            subtask1.join();
//            subtask2.join();
        }
    }

    public static void main(String[] args) {
        int[] array = {1, 7, 4, 3, 6};
        doIt(array, 2, 5);



        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
    }
}
