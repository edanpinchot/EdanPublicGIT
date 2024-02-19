package edu.yu.introtoalgs;

import java.util.LinkedList;

public class Digraph {
    private int V;
    private int E;
    //private Bag<Integer>[] adj;
    private LinkedList<Integer>[] adj;

    public Digraph(int V) {
        this.V = V;
        this.E = 0;
        //adj = (Bag<Integer>[]) new Bag[V];
        adj = (LinkedList<Integer>[]) new LinkedList[V];
        for (int i = 0; i < V; i++) {
            //adj[V] = new Bag<Integer>();
            adj[i] = new LinkedList<Integer>();
        }
    }

    public int V() {
        return V;
    }

    public int E() {
        return E;
    }

    public void addEdge(int v, int w) {
        adj[v].add(w);
        E++;
    }

    public Iterable<Integer> adj(int v) {
        return adj[v];
    }

    public Digraph reverse() {
        Digraph R = new Digraph(V);
        for (int v = 0; v < V; v++) {
            for (int w : adj(v)) {
                R.addEdge(w, v);
            }
        }
        return R;
    }
}
