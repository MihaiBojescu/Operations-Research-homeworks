package com.mihaibojescu.solvers.branch_and_bound.util;

import java.util.List;

public class Graph {
    private int numberOfVertices;
    private int numberOfEdges;
    private List<int[]> edges;

    public Graph(int numberOfVertices, int numberOfEdges, List<int[]> edges) {
        this.numberOfVertices = numberOfVertices;
        this.numberOfEdges = numberOfEdges;
        this.edges = edges;
    }

    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public int getNumberOfEdges() {
        return numberOfEdges;
    }

    public List<int[]> getEdges() {
        return edges;
    }
}