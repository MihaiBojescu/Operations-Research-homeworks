package com.mihaibojescu.solvers.branch_and_bound.math;

import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.mihaibojescu.solvers.branch_and_bound.interfaces.Solver;
import com.mihaibojescu.solvers.branch_and_bound.util.Box;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;

public class ParallelBranchAndBoundSolverRunnable implements Runnable {
    private Solver solver;
    private double tolerance;
    private CountDownLatch latch;
    private BlockingQueue<Problem> input;
    private BlockingQueue<Problem> output;
    private ConcurrentHashMap<String, Boolean> visited;
    private Box<Result> solution;
    private boolean debug;

    public ParallelBranchAndBoundSolverRunnable(Solver solver,
            double tolerance,
            CountDownLatch latch,
            BlockingQueue<Problem> input,
            BlockingQueue<Problem> output,
            ConcurrentHashMap<String, Boolean> visited,
            Box<Result> solution)
            throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        this.solver = solver;
        this.tolerance = tolerance;
        this.latch = latch;
        this.input = input;
        this.output = output;
        this.visited = visited;
        this.solution = solution;
        this.debug = false;
    }

    public ParallelBranchAndBoundSolverRunnable(Solver solver,
            double tolerance,
            CountDownLatch latch,
            BlockingQueue<Problem> input,
            BlockingQueue<Problem> output,
            ConcurrentHashMap<String, Boolean> visited,
            Box<Result> solution,
            boolean debug)
            throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        this.solver = solver;
        this.tolerance = tolerance;
        this.latch = latch;
        this.input = input;
        this.output = output;
        this.visited = visited;
        this.solution = solution;
        this.debug = debug;
    }

    @Override
    public void run() {
        try {
            Problem problem = this.input.poll();

            if (problem == null) {
                return;
            }

            Result result = this.solver.run(problem);

            this.log(MessageFormat.format("\nStatistics: {0} subproblems left, {1} subproblems visited",
                    this.input.size() + this.output.size(),
                    visited.size()));
            this.log(MessageFormat.format("\tResult: {0}, with values: {1}",
                    result.getObjectiveValue(),
                    Arrays.toString(result.getSolution())));

            String problemSignature = this.getProblemSignature(problem);

            synchronized (this.visited) {
                if (visited.getOrDefault(problemSignature, false)) {
                    this.log("\tAlready visited: true");
                    this.log("\tSolution is integral: null");
                    this.log("\tBiggest fractional value: null, index -1");
                    return;
                }
                
                this.log("\tAlready visited: false");

                visited.putIfAbsent(problemSignature, true);
            }

            synchronized (this.solution) {
                Result bestResult = this.solution.getValue();

                if (result.getObjectiveValue() == Solver.INF) {
                    this.solution.setValue(new Result(null, Solver.INF));
                }

                if (result.getObjectiveValue() <= bestResult.getObjectiveValue()) {
                    return;
                }

                if (isSolutionIntegral(result)) {
                    this.log("\tSolution is integral: true");
                    this.log("\tBiggest fractional value: null, index -1");
                    this.solution.setValue(result);
                    return;
                }

                this.log("\tSolution is integral: false");
            }

            int biggestFractionalVariableIndex = this.getBiggestFractionalVariableIndex(result);
            this.log(MessageFormat.format("\tBiggest fractional value: {0}, index {1}",
                    result.getSolution()[biggestFractionalVariableIndex], biggestFractionalVariableIndex));

            if (biggestFractionalVariableIndex >= 0) {

                Problem subProblem1 = problem.clone();
                Problem subProblem2 = problem.clone();

                subProblem1.addConstraint(
                        this.createConstraint(problem, biggestFractionalVariableIndex,
                                1.0),
                        Math.floor(result.getSolution()[biggestFractionalVariableIndex]));

                subProblem2.addConstraint(
                        this.createConstraint(problem, biggestFractionalVariableIndex,
                                -1.0),
                        Math.ceil(result.getSolution()[biggestFractionalVariableIndex]));

                this.output.add(subProblem1);
                this.output.add(subProblem2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.latch.countDown();
        }
    }

    private void log(String string) {
        if (!this.debug) {
            return;
        }

        System.out.println(string);
    }

    private String getProblemSignature(Problem problem) {
        StringBuilder signature = new StringBuilder();

        for (double[] row : problem.getConstraints().toRawMatrix()) {
            signature.append(Arrays.toString(row)).append(";");
        }

        signature.append(Arrays.toString(problem.getBounds().toRawVector()));
        return signature.toString();
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
