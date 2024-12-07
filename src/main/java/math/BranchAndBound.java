package src.main.java.math;

import java.util.Stack;
import src.main.java.interfaces.Solver;
import src.main.java.util.Result;

public class BranchAndBound implements Solver {
    private Solver solver;
    private double tolerance;

    public BranchAndBound(Solver solver, double tolerance) throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        this.solver = solver;
        this.tolerance = tolerance;
    }

    public Result run(Problem problem) throws Exception {
        Stack<Problem> stack = new Stack<>();
        stack.push(problem);

        Result bestResult = new Result(null, -Solver.INF);

        while (!stack.isEmpty()) {
            Problem currentProblem = stack.pop();
            Result result = this.solver.run(currentProblem);

            if (result.getObjectiveValue() == Solver.INF) {
                return new Result(null, Solver.INF);
            }

            if (result.getObjectiveValue() <= bestResult.getObjectiveValue()) {
                continue;
            }

            if (isSolutionIntegral(result)) {
                bestResult = result;
                continue;
            }

            int biggestFractionalVariableIndex = this.getBiggestFractionalVariableIndex(result);

            if (biggestFractionalVariableIndex >= 0) {
                Problem SubProblem1 = currentProblem.clone();
                Problem SubProblem2 = currentProblem.clone();

                SubProblem1.addConstraint(
                        this.createConstraint(currentProblem, biggestFractionalVariableIndex,
                                1.0),
                        Math.floor(result.getSolution()[biggestFractionalVariableIndex]));

                SubProblem2.addConstraint(
                        this.createConstraint(currentProblem, biggestFractionalVariableIndex,
                                -1.0),
                        Math.ceil(result.getSolution()[biggestFractionalVariableIndex]));

                stack.push(SubProblem1);
                stack.push(SubProblem2);
            }
        }

        return bestResult;
    }

    private boolean isSolutionIntegral(Result result) {
        boolean isIntegral = true;

        for (double x : result.getSolution()) {
            if (x - Math.floor(x) > this.tolerance) {
                isIntegral = false;
                break;
            }
        }

        return isIntegral;
    }

    private int getBiggestFractionalVariableIndex(Result result) {
        int index = -1;
        double maxFraction = 0.0;
        double[] solution = result.getSolution();

        for (int i = 0; i < solution.length; i++) {
            double fraction = solution[i] - Math.floor(solution[i]);

            if (fraction > maxFraction) {
                maxFraction = fraction;
                index = i;
            }
        }

        return index;
    }

    private double[] createConstraint(Problem problem, int variableIndex, double coefficient) {
        double[] constraintMultipliers = new double[problem.getNumberOfVariables()];
        constraintMultipliers[variableIndex] = coefficient;
        return constraintMultipliers;
    }
}
