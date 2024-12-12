package com.mihaibojescu.solvers.branch_and_bound.math;

import java.util.ArrayList;
import java.util.List;

public class CuttingPlaneSolver {
    private static final double EPSILON = 1e-6;
    private static final int MAX_ITERATIONS = 1000;

    private double[][] coefficients;
    private double[] rhs;
    private double[] objective;
    private int numVariables;
    private int numConstraints;

    public CuttingPlaneSolver(double[][] coefficients, double[] rhs, double[] objective) {
        this.coefficients = coefficients;
        this.rhs = rhs;
        this.objective = objective;
        this.numVariables = objective.length;
        this.numConstraints = rhs.length;
    }

    public double[] solve() {
        List<double[]> additionalConstraints = new ArrayList<>();
        List<Double> additionalRhs = new ArrayList<>();

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            TwoPhaseSimplexSolver solver = new TwoPhaseSimplexSolver(getCurrentCoefficients(additionalConstraints),
                    getCurrentRhs(additionalRhs), objective);

            if (!solver.isFeasible()) {
                System.out.println("Infeasible problem");
                return null;
            }

            double[] solution = solver.solve();

            if (isIntegerSolution(solution)) {
                return solution;
            }

            int nonIntegerIndex = findNonIntegerIndex(solution);
            double[] gomoryFractionalCut = this.generateGomoryFractionalCut(solver, nonIntegerIndex);

            additionalConstraints.add(gomoryFractionalCut);
            additionalRhs.add(Math.floor(solver.getTableauValue(nonIntegerIndex, solver.getNumColumns() - 1)));
        }

        System.out.println("Max iterations reached without finding an integer solution");
        return null;
    }

    private double[][] getCurrentCoefficients(List<double[]> additionalConstraints) {
        double[][] currentCoefficients = new double[numConstraints + additionalConstraints.size()][numVariables];

        for (int i = 0; i < numConstraints; i++) {
            System.arraycopy(coefficients[i], 0, currentCoefficients[i], 0, numVariables);
        }

        for (int i = 0; i < additionalConstraints.size(); i++) {
            System.arraycopy(additionalConstraints.get(i), 0, currentCoefficients[numConstraints + i], 0, numVariables);
        }

        return currentCoefficients;
    }

    private double[] getCurrentRhs(List<Double> additionalRhs) {
        double[] currentRhs = new double[numConstraints + additionalRhs.size()];
        System.arraycopy(rhs, 0, currentRhs, 0, numConstraints);
        for (int i = 0; i < additionalRhs.size(); i++) {
            currentRhs[numConstraints + i] = additionalRhs.get(i);
        }
        return currentRhs;
    }

    private boolean isIntegerSolution(double[] solution) {
        for (double value : solution) {
            if (Math.abs(value - Math.round(value)) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    private int findNonIntegerIndex(double[] solution) {
        for (int i = 0; i < solution.length; i++) {
            if (Math.abs(solution[i] - Math.round(solution[i])) > EPSILON) {
                return i;
            }
        }
        return -1;
    }

    private double[] generateGomoryFractionalCut(TwoPhaseSimplexSolver solver, int nonIntegerIndex) {
        double[] cut = new double[numVariables];
        for (int j = 0; j < numVariables; j++) {
            double aij = solver.getTableauValue(nonIntegerIndex, j);
            cut[j] = Math.floor(aij) - aij;
        }
        return cut;
    }
}