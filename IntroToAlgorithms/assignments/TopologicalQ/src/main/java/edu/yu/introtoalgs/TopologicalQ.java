package edu.yu.introtoalgs;

import java.util.*;

public class TopologicalQ {
    private int[] indegrees;
    private Queue<Integer> theQueue;
    private LinkedList<Integer> topologicallyOrdered;
    private int count;
    private int V;

    //This constructor determines whether the digraph has a cycle or not.
    //If it does not have a cycle, the constructor determines a valid topological sort for the digraph.
    public TopologicalQ(Digraph G) {
        this.indegrees = new int[G.V()];
        this.theQueue = new LinkedList<Integer>();
        this.topologicallyOrdered = new LinkedList<Integer>();
        this.count = 0;
        this.V = G.V();

        //first fill indegrees with zero's
        for (int i = 0; i < G.V(); i++) {
            indegrees[i] = 0;
        }
        //then calculate each vertex's indegree
        for (int i = 0; i < G.V(); i++) {
            Iterable<Integer> edges = G.adj(i);
            for (Integer p : edges) {
                indegrees[p]++;
            }
        }

        //add vertex's with initial indegree of 0 to the queue
        for (int i = 0; i < indegrees.length; i++) {
            if (indegrees[i] == 0) {
                theQueue.add(i);
            }
        }

        //as long as queue isn't empty, add next item to final ordered array
        while(!theQueue.isEmpty()) {
            int p = theQueue.poll();
            topologicallyOrdered.add(p);

            //decrement removed vertex's neighbors indegrees, adding them to the queue if the decrement gives them indegree of 0
            for (int neighbors : G.adj(p)) {
                indegrees[neighbors]--;
                if (indegrees[neighbors] == 0) {
                    theQueue.add(neighbors);
                }
            }
            //keep track of number of vertices that have been queued (vertices in a cycle will never be added onto the queue)
            count++;
        }
    }


    //This method returns true iff G has a topological order, false otherwise.
    public boolean hasOrder() {
        //if all the vertices were not queued, then there is a cycle and no topological order
        if (count != V) {
            return false;
        }
        else {
            return true;
        }
    }


    //This method returns an java.util.Iterable allowing you to do a “for each” iteration over the vertices in a valid topological sort.
    //The method returns null if no topological order exists.
    public Iterable<Integer> order() {
        if (!hasOrder()) {
            return null;
        }
        return topologicallyOrdered;
    }

    public static void main(String[] args) {
        Digraph dg = new Digraph(10);
        dg.addEdge(2, 3);
        dg.addEdge(2, 7);
        dg.addEdge(6, 9);
        dg.addEdge(0, 1);
        dg.addEdge(5, 6);
        dg.addEdge(3, 4);
        dg.addEdge(6, 7);
        dg.addEdge(1, 2);
        dg.addEdge(8, 9);
        dg.addEdge(0, 5);
        dg.addEdge(4, 7);
        dg.addEdge(4, 5);
        dg.addEdge(1, 3);
        dg.addEdge(7, 8);
        dg.addEdge(4, 9);
        dg.addEdge(3, 6);

        TopologicalQ tq = new TopologicalQ(dg);
        Iterable<Integer> returnIt = tq.order();
        if (returnIt == null) {
            System.out.println("ERROR: Cycle Found!");
        }
        else {
            for (int vertex : returnIt) {
                System.out.println(vertex);
            }
        }
    }
}
