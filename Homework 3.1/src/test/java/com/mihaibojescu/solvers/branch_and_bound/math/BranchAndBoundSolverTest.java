package com.mihaibojescu.solvers.branch_and_bound.math;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mihaibojescu.solvers.branch_and_bound.interfaces.Solver;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;
import com.mihaibojescu.solvers.branch_and_bound.util.Graph;

public class BranchAndBoundSolverTest {
    @Test
    public void toyExample() throws Exception {
        Problem problem = new Problem(
                new Matrix(new double[] { 2, 3 }), new Matrix(new double[][] { { 3, 2 }, { 4, 5 }, }),
                new Matrix(new double[] { 13, 11 }));
        Solver twoPhaseSolver = new TwoPhaseSimplexSolverAdapter();
        Solver branchAndBound = new ParallelBranchAndBoundSolver(twoPhaseSolver, 0.0001, 8);
        Result result = branchAndBound.run(problem);

        assertArrayEquals(result.getSolution(), new double[] { 0.0, 2.0 });
        assertEquals(result.getObjectiveValue(), 6.0);
    }

    @Test
    public void reddyMikksExample() throws Exception {
        Problem problem = new Problem(
                new Matrix(new double[] { 5, 4 }),
                new Matrix(new double[][] { { 6, 4 }, { 1, 2 }, { -1, 1 }, { 0, 1 } }),
                new Matrix(new double[] { 24, 6, 1, 2 }));
        Solver twoPhaseSolver = new TwoPhaseSimplexSolverAdapter();
        Solver branchAndBound = new ParallelBranchAndBoundSolver(twoPhaseSolver, 0.0001, 8);
        Result result = branchAndBound.run(problem);

        assertArrayEquals(result.getSolution(), new double[] { 3.0, 1.0000000000000004 });
        assertEquals(result.getObjectiveValue(), 19.0);
    }

    @Test
    public void dsjc125_1_colExample() throws Exception {
        Graph graph = this.parseGraph("/dsjc125.1.col");
        Problem problem = this.buildProblemFromGraph(graph);

        Solver twoPhaseSolver = new TwoPhaseSimplexSolverAdapter();
        Solver branchAndBound = new ParallelBranchAndBoundSolver(twoPhaseSolver, 0.0001, 8, true);
        Result result = branchAndBound.run(problem);

        System.out.println(Arrays.toString(result.getSolution()));
    }

    @Test
    public void queens5_5_colExample() throws Exception {
        Graph graph = this.parseGraph("/queen5_5.col");
        Problem problem = this.buildProblemFromGraph(graph);

        Solver twoPhaseSolver = new TwoPhaseSimplexSolverAdapter();
        Solver branchAndBound = new ParallelBranchAndBoundSolver(twoPhaseSolver, 0.0001, 8, true);
        Result result = branchAndBound.run(problem);

        System.out.println(Arrays.toString(result.getSolution()));
    }

    private Graph parseGraph(String resource) throws IOException, URISyntaxException {
        URL fileUrl = this.getClass().getResource(resource);
        File file = new File(fileUrl.toURI());
        List<String> data = Files.readAllLines(file.toPath());

        int numberOfVertices = -1;
        int numberOfEdges = -1;
        List<int[]> edges = new ArrayList<>();

        for (String line : data) {
            switch (line.charAt(0)) {
                case 'c':
                    break;
                case 'p': {
                    String[] parts = line.split(" ");
                    numberOfVertices = Integer.valueOf(parts[2]);
                    numberOfEdges = Integer.valueOf(parts[3]);
                    break;
                }
                case 'e': {
                    String[] parts = line.split(" ");
                    edges.add(new int[] { Integer.valueOf(parts[1]), Integer.valueOf(parts[2]) });
                    break;
                }
            }
        }

        assert edges.size() == numberOfEdges;

        return new Graph(numberOfVertices, numberOfEdges, edges);
    }

    private Problem buildProblemFromGraph(Graph graph) {
        Matrix objectiveFunctionMultipliers = new Matrix(1, graph.getNumberOfVertices());
        Matrix constraintsMultipliers = new Matrix(graph.getNumberOfEdges(), graph.getNumberOfVertices());
        Matrix bounds = new Matrix(1, graph.getNumberOfEdges());

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            objectiveFunctionMultipliers.set(0, i, 1);
        }

        for (int i = 0; i < graph.getNumberOfEdges(); i++) {
            for (int vertex : graph.getEdges().get(i)) {
                constraintsMultipliers.set(i, vertex - 1, 1);
            }
        }

        for (int i = 0; i < graph.getNumberOfEdges(); i++) {
            bounds.set(0, i, 1);
        }

        return new Problem(objectiveFunctionMultipliers, constraintsMultipliers, bounds);
    }
}
