import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Problem problem = new Problem(
                new Matrix(new double[] { 1, 1 }), new Matrix(new double[][] { { 1, 1 }, }),
                new Matrix(new double[] { 1 }));
        Solver twoPhaseSolver = new TwoPhaseSolver();
        Solver branchAndBound = new BranchAndBound(twoPhaseSolver, 0.0001);
        Result result = branchAndBound.run(problem);

        if (result.objectiveValue == Solver.INF) {
            System.out.println("The problem is unbounded.");
        } else if (result.objectiveValue == -Solver.INF) {
            System.out.println("The problem is infeasible.");
        } else {
            System.out.println("Optimal solution found: z = " + result.objectiveValue);
            System.out.println("Solution: " + Arrays.toString(result.solution));
        }
    }
}
