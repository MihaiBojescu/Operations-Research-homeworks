import java.util.Stack;

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
            Result result = currentProblem.getResult();

            if (result.objectiveValue == Solver.INF) {
                return new Result(null, Solver.INF);
            }

            if (result.objectiveValue <= bestResult.objectiveValue) {
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
                        Math.floor(result.solution[biggestFractionalVariableIndex]));

                SubProblem2.addConstraint(
                        this.createConstraint(currentProblem, biggestFractionalVariableIndex,
                                -1.0),
                        Math.ceil(result.solution[biggestFractionalVariableIndex]));

                stack.push(SubProblem1);
                stack.push(SubProblem2);
            }
        }

        return bestResult;
    }

    private boolean isSolutionIntegral(Result result) {
        boolean isIntegral = true;

        for (double x : result.solution) {
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

        for (int i = 0; i < result.solution.length; i++) {
            double fraction = result.solution[i] - Math.floor(result.solution[i]);

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
