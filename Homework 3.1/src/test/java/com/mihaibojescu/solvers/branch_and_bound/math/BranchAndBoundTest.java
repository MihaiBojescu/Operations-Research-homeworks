package com.mihaibojescu.solvers.branch_and_bound.math;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mihaibojescu.solvers.branch_and_bound.interfaces.Solver;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;

public class BranchAndBoundTest {
    @Test
    public void toyExample() throws Exception {
        Problem problem = new Problem(
                new Matrix(new double[] { 2, 3 }), new Matrix(new double[][] { { 3, 2 }, { 4, 5 }, }),
                new Matrix(new double[] { 13, 11 }));
        Solver twoPhaseSolver = new TwoPhaseSimplexSolverAdapter();
        Solver branchAndBound = new BranchAndBound(twoPhaseSolver, 0.0001);
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
        Solver branchAndBound = new BranchAndBound(twoPhaseSolver, 0.0001, true);
        Result result = branchAndBound.run(problem);

        assertArrayEquals(result.getSolution(), new double[] { 3.0, 1.0 });
        assertEquals(result.getObjectiveValue(), 19.0);
    }
}
