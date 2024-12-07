import java.util.Arrays;

import interfaces.Solver;
import math.BranchAndBound;
import math.Matrix;
import math.Problem;
import math.TwoPhaseSolver;
import util.Result;

public class Main {
    public static void main(String[] args) throws Exception {
        Problem problem = new Problem(
                new Matrix(new double[] { 1, 1 }), new Matrix(new double[][] { { 1, 1 }, }),
                new Matrix(new double[] { 1 }));
        Solver twoPhaseSolver = new TwoPhaseSolver();
        Solver branchAndBound = new BranchAndBound(twoPhaseSolver, 0.0001);
        Result result = branchAndBound.run(problem);

        if (result.getObjectiveValue() == Solver.INF) {
            System.out.println("The problem is unbounded.");
            return;
        }

        if (result.getObjectiveValue() == -Solver.INF) {
            System.out.println("The problem is infeasible.");
            return;
        }

        System.out.println("Optimal solution found: z = " + result.getObjectiveValue());
        System.out.println("Solution: " + Arrays.toString(result.getSolution()));
    }
}
