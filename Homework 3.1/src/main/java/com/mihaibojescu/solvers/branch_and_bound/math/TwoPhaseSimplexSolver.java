package com.mihaibojescu.solvers.branch_and_bound.math;

public class TwoPhaseSimplexSolver {
    private static final double TOLERANCE = 1.0E-8;

    private final double[][] tableau;

    private final int numConstraints;
    private final int numVariables;

    private final int[] basis;

    private boolean isFeasible;
    private boolean isUnbounded;

    public TwoPhaseSimplexSolver(double[][] coefficients, double[] rhs, double[] objective) {
        numConstraints = rhs.length;
        numVariables = objective.length;
        tableau = new double[numConstraints + 2][numVariables + numConstraints + numConstraints + 1];
        isFeasible = true;
        isUnbounded = false;

        // Initialize tableau and basis
        initializeTableau(coefficients, rhs, objective);
        basis = new int[numConstraints];
        for (int i = 0; i < numConstraints; i++) {
            basis[i] = numVariables + numConstraints + i;
        }
    }

    public double getTableauValue(int row, int col) {
        return tableau[row][col];
    }

    public int getNumColumns() {
        return tableau[0].length;
    }

    private void initializeTableau(double[][] coefficients, double[] rhs, double[] objective) {
        // Fill coefficients
        for (int i = 0; i < numConstraints; i++) {
            for (int j = 0; j < numVariables; j++) {
                tableau[i][j] = coefficients[i][j];
            }
        }

        // Slack variables
        for (int i = 0; i < numConstraints; i++) {
            tableau[i][numVariables + i] = 1.0;
        }

        // Right-hand sides
        for (int i = 0; i < numConstraints; i++) {
            tableau[i][numVariables + numConstraints + numConstraints] = rhs[i];
        }

        // Objective function
        for (int j = 0; j < numVariables; j++) {
            tableau[numConstraints][j] = objective[j];
        }

        // Handle artificial variables and Phase 1 objective
        for (int i = 0; i < numConstraints; i++) {
            if (rhs[i] < 0) {
                tableau[i][numVariables + numConstraints + numConstraints] = -rhs[i];
                for (int j = 0; j <= numVariables; j++) {
                    tableau[i][j] = -tableau[i][j];
                }
                tableau[i][numVariables + i] = -1.0;
            }
        }

        for (int i = 0; i < numConstraints; i++) {
            tableau[i][numVariables + numConstraints + i] = 1.0;
        }

        for (int i = 0; i < numConstraints; i++) {
            tableau[numConstraints + 1][numVariables + numConstraints + i] = -1.0;
        }
    }

    public double[] solve() {
        try {
            phase1();
            if (isFeasible) {
                phase2();
            }
        } catch (ArithmeticException e) {
            isFeasible = false;
            isUnbounded = true;
        }
        return primalSolution();
    }

    public double getObjectiveValue() {
        return optimalValue();
    }

    public boolean isFeasible() {
        return isFeasible;
    }

    public boolean isUnbounded() {
        return isUnbounded;
    }

    private void phase1() {
        while (true) {
            int enteringColumn = findEnteringColumnPhase1();
            if (enteringColumn == -1) break;

            int leavingRow = findLeavingRow(enteringColumn);
            if (leavingRow == -1) {
                throw new ArithmeticException("Linear program is infeasible");
            }

            pivot(leavingRow, enteringColumn);
            basis[leavingRow] = enteringColumn;
        }

        if (tableau[numConstraints + 1][numVariables + numConstraints + numConstraints] > TOLERANCE) {
            isFeasible = false;
        }

        eliminateArtificialVariables();
    }

    private void phase2() {
        while (true) {
            int enteringColumn = findEnteringColumnPhase2();
            if (enteringColumn == -1) break;

            int leavingRow = findLeavingRow(enteringColumn);
            if (leavingRow == -1) {
                throw new ArithmeticException("Linear program is unbounded");
            }

            pivot(leavingRow, enteringColumn);
            basis[leavingRow] = enteringColumn;
        }
    }

    private void eliminateArtificialVariables() {
        for (int i = 0; i < numConstraints; i++) {
            int basicVariable = basis[i];
            if (basicVariable >= numVariables + numConstraints) {
                boolean pivoted = false;

                for (int j = 0; j < numVariables + numConstraints; j++) {
                    if (!isArtificial(j) && tableau[i][j] != 0) {
                        pivot(i, j);
                        basis[i] = j;
                        pivoted = true;
                        break;
                    }
                }

                if (!pivoted) {
                    removeRowAndColumn(i, basicVariable);
                }
            }
        }
    }

    private boolean isArtificial(int variableIndex) {
        return variableIndex >= numVariables + numConstraints;
    }

    private void removeRowAndColumn(int rowIndex, int colIndex) {
        for (int i = 0; i <= numConstraints + 1; i++) {
            tableau[i][colIndex] = 0;
        }

        for (int j = 0; j <= numVariables + numConstraints + numConstraints; j++) {
            tableau[rowIndex][j] = 0;
        }
    }

    private int findEnteringColumnPhase1() {
        for (int j = 0; j < numVariables+numConstraints; j++)
            if (tableau[numConstraints+1][j] > TOLERANCE) return j;
        return -1;  // Optimal solution for Phase 1
    }

    private int findEnteringColumnPhase2() {
        for (int j = 0; j < numVariables+numConstraints; j++)
            if (tableau[numConstraints][j] > TOLERANCE) return j;
        return -1;  // Optimal solution for Phase 2
    }

    private int findLeavingRow(int enteringColumn) {
        int leavingRow = -1;
        for (int i = 0; i < numConstraints; i++) {
            if (tableau[i][enteringColumn] <= TOLERANCE) continue;
            else if (leavingRow == -1) leavingRow = i;
            else if ((tableau[i][numVariables+numConstraints+numConstraints] / tableau[i][enteringColumn])
                    < (tableau[leavingRow][numVariables+numConstraints+numConstraints] / tableau[leavingRow][enteringColumn]))
                leavingRow = i;
        }
        return leavingRow;
    }

    private void pivot(int leavingRow, int enteringColumn) {
        for (int i = 0; i <= numConstraints+1; i++)
            for (int j = 0; j <= numVariables+numConstraints+numConstraints; j++)
                if (i != leavingRow && j != enteringColumn)
                    tableau[i][j] -= tableau[leavingRow][j] * (tableau[i][enteringColumn] / tableau[leavingRow][enteringColumn]);

        for (int i = 0; i <= numConstraints+1; i++)
            if (i != leavingRow) tableau[i][enteringColumn] = 0.0;

        for (int j = 0; j <= numVariables+numConstraints+numConstraints; j++)
            if (j != enteringColumn) tableau[leavingRow][j] /= tableau[leavingRow][enteringColumn];
        tableau[leavingRow][enteringColumn] = 1.0;
    }

    public double optimalValue() {
        return -tableau[numConstraints][numVariables+numConstraints+numConstraints];
    }

    public double[] primalSolution() {
        double[] solution = new double[numVariables];
        for (int i = 0; i < numConstraints; i++)
            if (basis[i] < numVariables) solution[basis[i]] = tableau[i][numVariables+numConstraints+numConstraints];
        return solution;
    }

    public double[] dualSolution() {
        double[] dual = new double[numConstraints];
        for (int i = 0; i < numConstraints; i++) {
            dual[i] = -tableau[numConstraints][numVariables+i];
            if (dual[i] == -0.0) dual[i] = 0.0;
        }
        return dual;
    }
}
