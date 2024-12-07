package math;

import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import interfaces.Solver;
import util.Result;

public class BranchAndBound implements Solver {
    private Solver solver;
    private double tolerance;
    private boolean debug;

    public BranchAndBound(Solver solver, double tolerance) throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        this.solver = solver;
        this.tolerance = tolerance;
        this.debug = false;
    }

    public BranchAndBound(Solver solver, double tolerance, boolean debug) throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        this.solver = solver;
        this.tolerance = tolerance;
        this.debug = debug;
    }

    @Override
    public Result run(Problem problem) throws Exception {
        Deque<Problem> queue = new ArrayDeque<>();
        queue.push(problem);

        Result bestResult = new Result(null, -Solver.INF);

        while (!queue.isEmpty()) {
            Problem currentProblem = queue.removeFirst();
            Result result = this.solver.run(currentProblem);

            this.log(MessageFormat.format("Result: {0}, with values: {1}", result.getObjectiveValue(),
                    Arrays.toString(result.getSolution())));

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

                queue.addLast(SubProblem1);
                queue.addLast(SubProblem2);
            }
        }

        return bestResult;
    }

    private void log(String string) {
        if (!this.debug) {
            return;
        }

        System.out.println(string);
    }

    private boolean isSolutionIntegral(Result result) {
        boolean isIntegral = true;
        double[] solution = result.getSolution();

        for (double x : solution) {
            if (x - Math.floor(x) > this.tolerance) {
                isIntegral = false;
                break;
            }
        }

        this.log(MessageFormat.format("\tIs problem integral: {0}", isIntegral));

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

        this.log(MessageFormat.format("\tBiggest fractional index: {0}", index));

        return index;
    }

    private double[] createConstraint(Problem problem, int variableIndex, double coefficient) {
        double[] constraintMultipliers = new double[problem.getNumberOfVariables()];
        constraintMultipliers[variableIndex] = coefficient;
        return constraintMultipliers;
    }
}
