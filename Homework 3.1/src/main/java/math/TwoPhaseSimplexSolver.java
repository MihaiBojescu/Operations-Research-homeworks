package math;

public class TwoPhaseSimplexSolver {
    private static final double TOLERANCE = 1.0E-8;

    private final double[][] tableau;

    private final int numConstraints;
    private final int numVariables;

    private final int[] basis;

    public TwoPhaseSimplexSolver(double[][] coefficients, double[] rhs, double[] objective) {
        numConstraints = rhs.length;
        numVariables = objective.length;
        tableau = new double[numConstraints+2][numVariables+numConstraints+numConstraints+1];

        for (int i = 0; i < numConstraints; i++)
            for (int j = 0; j < numVariables; j++)
                tableau[i][j] = coefficients[i][j];

        for (int i = 0; i < numConstraints; i++)
            tableau[i][numVariables+i] = 1.0;

        for (int i = 0; i < numConstraints; i++)
            tableau[i][numVariables+numConstraints+numConstraints] = rhs[i];

        for (int j = 0; j < numVariables; j++)
            tableau[numConstraints][j] = objective[j];

        for (int i = 0; i < numConstraints; i++) {
            if (rhs[i] < 0) {
                tableau[i][numVariables+numConstraints+numConstraints] = -rhs[i];
                for (int j = 0; j <= numVariables; j++)
                    tableau[i][j] = -tableau[i][j];
                tableau[i][numVariables+i] = -1.0;
            }
        }

        // Initialize artificial variables in the basis
        for (int i = 0; i < numConstraints; i++)
            tableau[i][numVariables+numConstraints+i] = 1.0;
        for (int i = 0; i < numConstraints; i++)
            tableau[numConstraints+1][numVariables+numConstraints+i] = -1.0;
        for (int i = 0; i < numConstraints; i++)
            pivot(i, numVariables+numConstraints+i);

        basis = new int[numConstraints];
        for (int i = 0; i < numConstraints; i++)
            basis[i] = numVariables + numConstraints + i;

        try {
            phase1();
            phase2();
        } catch (ArithmeticException arithmeticException) {
            System.out.println("Linear program is infeasible");
        }
    }

    private void phase1() {
        while (true) {
            int enteringColumn = findEnteringColumnPhase1();
            if (enteringColumn == -1)
                break;

            int leavingRow = findLeavingRow(enteringColumn);
            assert leavingRow != -1 : "Entering column = " + enteringColumn;

            pivot(leavingRow, enteringColumn);

            basis[leavingRow] = enteringColumn;
        }
        if (tableau[numConstraints + 1][numVariables + numConstraints + numConstraints] > TOLERANCE)
            throw new ArithmeticException("Linear program is infeasible");
    }

    private void phase2() {
        while (true) {
            int enteringColumn = findEnteringColumnPhase2();
            if (enteringColumn == -1)
                break; // Optimal solution found for phase 2

            int leavingRow = findLeavingRow(enteringColumn);
            if (leavingRow == -1)
                throw new ArithmeticException("Linear program is unbounded");

            pivot(leavingRow, enteringColumn);

            basis[leavingRow] = enteringColumn;
        }
    }

    private int findEnteringColumnPhase1() {
        for (int j = 0; j < numVariables + numConstraints; j++)
            if (tableau[numConstraints + 1][j] > TOLERANCE)
                return j;
        return -1; // Optimal solution for Phase 1
    }

    private int findEnteringColumnPhase2() {
        for (int j = 0; j < numVariables + numConstraints; j++)
            if (tableau[numConstraints][j] > TOLERANCE)
                return j;
        return -1; // Optimal solution for Phase 2
    }

    private int findLeavingRow(int enteringColumn) {
        int leavingRow = -1;
        for (int i = 0; i < numConstraints; i++) {
            if (tableau[i][enteringColumn] <= TOLERANCE)
                continue;
            else if (leavingRow == -1)
                leavingRow = i;
            else if ((tableau[i][numVariables + numConstraints + numConstraints]
                    / tableau[i][enteringColumn]) < (tableau[leavingRow][numVariables + numConstraints + numConstraints]
                            / tableau[leavingRow][enteringColumn]))
                leavingRow = i;
        }
        return leavingRow;
    }

    private void pivot(int leavingRow, int enteringColumn) {
        for (int i = 0; i <= numConstraints + 1; i++)
            for (int j = 0; j <= numVariables + numConstraints + numConstraints; j++)
                if (i != leavingRow && j != enteringColumn)
                    tableau[i][j] -= tableau[leavingRow][j]
                            * (tableau[i][enteringColumn] / tableau[leavingRow][enteringColumn]);

        for (int i = 0; i <= numConstraints + 1; i++)
            if (i != leavingRow)
                tableau[i][enteringColumn] = 0.0;

        for (int j = 0; j <= numVariables + numConstraints + numConstraints; j++)
            if (j != enteringColumn)
                tableau[leavingRow][j] /= tableau[leavingRow][enteringColumn];
        tableau[leavingRow][enteringColumn] = 1.0;
    }

    public double optimalValue() {
        return -tableau[numConstraints][numVariables + numConstraints + numConstraints];
    }

    public double[] primalSolution() {
        double[] solution = new double[numVariables];
        for (int i = 0; i < numConstraints; i++)
            if (basis[i] < numVariables)
                solution[basis[i]] = tableau[i][numVariables + numConstraints + numConstraints];
        return solution;
    }

    public double[] dualSolution() {
        double[] dual = new double[numConstraints];
        for (int i = 0; i < numConstraints; i++) {
            dual[i] = -tableau[numConstraints][numVariables + i];
            if (dual[i] == -0.0)
                dual[i] = 0.0;
        }
        return dual;
    }

    private boolean check(double[][] coefficients, double[] rhs, double[] objective) {
        return isPrimalFeasible(coefficients, rhs) && isDualFeasible(coefficients, objective)
                && isOptimal(rhs, objective);
    }

    private boolean isPrimalFeasible(double[][] coefficients, double[] rhs) {
        double[] solution = primalSolution();
        for (int j = 0; j < solution.length; j++) {
            if (solution[j] < -TOLERANCE)
                return false;
        }

        for (int i = 0; i < numConstraints; i++) {
            double sum = 0.0;
            for (int j = 0; j < numVariables; j++) {
                sum += coefficients[i][j] * solution[j];
            }
            if (sum > rhs[i] + TOLERANCE)
                return false;
        }
        return true;
    }

    private boolean isDualFeasible(double[][] coefficients, double[] objective) {
        double[] dual = dualSolution();
        for (int i = 0; i < dual.length; i++) {
            if (dual[i] < -TOLERANCE)
                return false;
        }

        for (int j = 0; j < numVariables; j++) {
            double sum = 0.0;
            for (int i = 0; i < numConstraints; i++) {
                sum += coefficients[i][j] * dual[i];
            }
            if (sum < objective[j] - TOLERANCE)
                return false;
        }
        return true;
    }

    private boolean isOptimal(double[] rhs, double[] objective) {
        double[] solution = primalSolution();
        double[] dual = dualSolution();
        double value = optimalValue();

        double objectiveValue1 = 0.0;
        for (int j = 0; j < solution.length; j++)
            objectiveValue1 += objective[j] * solution[j];
        double objectiveValue2 = 0.0;
        for (int i = 0; i < dual.length; i++)
            objectiveValue2 += dual[i] * rhs[i];
        return Math.abs(value - objectiveValue1) <= TOLERANCE && Math.abs(value - objectiveValue2) <= TOLERANCE;
    }
}
