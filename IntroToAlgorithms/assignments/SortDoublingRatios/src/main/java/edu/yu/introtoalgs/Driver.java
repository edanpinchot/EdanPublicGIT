package edu.yu.introtoalgs;

import java.util.Random;

public class Driver {

    public static void main(String[] args) {

        class Number implements Comparable<Number>{
            private int value;

            public Number(int value) {
                this.value = value;
            }

            public int compareTo(Number o) {
                return this.value - o.value;
            }
        }

        class Stopwatch {
            private final long start;

            public Stopwatch() {
                start = System.currentTimeMillis();
            }

            public double elapsedTime() {
                long now = System.currentTimeMillis();
                return (now - start) / 1000.0;
            }
        }

        class JDKDoublingTest {
            public double timeTrial(int n) {
                Sorter bubbleSort = SortImplementations.SortFactory(SortImplementations.JDKSort);
                Comparable[] a = new Comparable[n];
                Random random = new Random();
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt(1000);
                }
                Stopwatch timer = new Stopwatch();
                bubbleSort.sortIt(a);
                return timer.elapsedTime();
            }
        }

        class BubbleDoublingTest {
            public double timeTrial(int n) {
                Sorter bubbleSort = SortImplementations.SortFactory(SortImplementations.Bubblesort);
                Comparable[] a = new Comparable[n];
                Random random = new Random();
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt(1000);
                }
                Stopwatch timer = new Stopwatch();
                bubbleSort.sortIt(a);
                return timer.elapsedTime();
            }
        }

        //back to main
        System.out.println("JDK sort doubling test from 1,000 to 50,000,000 (sorting numbers between 1 and 1,000)");
        JDKDoublingTest jdkDoublingTest = new JDKDoublingTest();
        double prev = jdkDoublingTest.timeTrial(500);
        for (int i = 1000; i < 50000000; i *= 2) {
            double time = jdkDoublingTest.timeTrial(i);
            System.out.println(i + ", " + time + ", " + (time/prev));
            prev = time;
        }
        System.out.println();

        System.out.println("Bubble sort doubling test from 250 to 100,000 (sorting numbers between 1 and 1,000)");
        BubbleDoublingTest bubbleDoublingTest = new BubbleDoublingTest();
        double prev2 = bubbleDoublingTest.timeTrial(125);
        for (int i = 250; i < 100000; i *= 2) {
            double time = bubbleDoublingTest.timeTrial(i);
            System.out.println(i + ", " + time + ", " + (time/prev2));
            prev2 = time;
        }
    }
}