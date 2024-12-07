package interfaces;

import math.Problem;
import util.Result;

public interface Solver {
    public static final double INF = Double.POSITIVE_INFINITY;
    public Result run(Problem problem) throws Exception;
}
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
                new Matrix(new double[] { 2, 3 }), new Matrix(new double[][] { { 3, 2 }, { 4, 5 }, }),
                new Matrix(new double[] { 13, 11 }));
        Solver twoPhaseSolver = new TwoPhaseSolver();
        Solver branchAndBound = new BranchAndBound(twoPhaseSolver, 0.0001, true);
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

            if (result.getObjectiveValue() < bestResult.getObjectiveValue()) {
                continue;
            }

            if (isSolutionIntegral(result)) {
                if (result.getObjectiveValue() > bestResult.getObjectiveValue()) {
                    bestResult = result;
                }

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
package math;

import java.text.MessageFormat;

public class Matrix {
    private int rows;
    private int cols;
    double[][] data;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.data[i][j] = 0;
            }
        }
    }

    public Matrix(double[] data) {
        this.data = new double[1][data.length];
        this.rows = 1;
        this.cols = data.length;

        System.arraycopy(data, 0, this.data[0], 0, this.cols);
    }

    public Matrix(double[][] data) {
        this.data = data;
        this.rows = data.length;
        this.cols = data[0].length;
    }

    public Matrix clone() {
        double[][] newData = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(this.data[i], 0, newData[i], 0, this.cols);
        }

        return new Matrix(newData);
    }

    public boolean isVector() {
        return this.cols == 1 || this.rows == 1;
    }

    public double[][] toRawMatrix() {
        return this.data;
    }

    public double[] toRawVector() throws IllegalStateException {
        if (!this.isVector()) {
            throw new IllegalStateException("The matrix is not a vector");
        }

        return this.data[0];
    }

    public int getNumberOfRows() {
        return this.rows;
    }

    public int getNumberOfColumns() {
        return this.cols;
    }

    public Matrix addRow(double[] data) throws IllegalArgumentException {
        if (data.length != this.cols) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The number of elements of the new row must match the number of columns: {0} is different than {1}",
                    data.length, this.cols - 1));
        }

        double[][] newData = new double[this.rows + 1][this.cols];

        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(this.data[i], 0, newData[i], 0, this.cols);
        }

        for (int i = 0; i < data.length; i++) {
            newData[newData.length - 1][i] = data[i];
        }

        return new Matrix(newData);
    }

    public Matrix addColumn(double[] data) throws IllegalArgumentException {
        if (data.length != this.rows) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The number of elements of the new column must match the number of rows: {0} is different than {1}",
                    data.length, this.rows - 1));
        }

        double[][] newData = new double[this.rows][this.cols + 1];

        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(this.data[i], 0, newData[i], 0, this.cols);
        }

        for (int i = 0; i < data.length; i++) {
            newData[i][newData[i].length - 1] = data[i];
        }

        return new Matrix(newData);
    }

    public double get(int row, int col) throws IllegalArgumentException {
        if (row < 0 || row >= this.rows - 1 || col < 0 || col >= this.cols - 1) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Index out of range: ({0}, {1}) must be between (0, 0) and ({2}, {3})", row,
                            col, this.rows - 1, this.cols - 1));
        }

        return this.data[row][col];
    }

    public void set(int row, int col, double value) throws IllegalArgumentException {
        if (row < 0 || row >= this.rows - 1 || col < 0 || col >= this.cols - 1) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Index out of range: ({0}, {1}) must be between (0, 0) and ({2}, {3})", row,
                            col, this.rows - 1, this.cols - 1));
        }

        this.data[row][col] = value;
    }

    public double[] getRow(int row) throws IllegalArgumentException {
        if (row < 0 || row >= this.rows - 1) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Index out of range: Row {0} must be between 0 and {1}", row, this.rows - 1));
        }

        double[] result = new double[this.cols];

        for (int i = 0; i < this.cols; i++) {
            result[i] = this.data[row][i];
        }

        return result;
    }

    public double[] getColumn(int col) throws IllegalArgumentException {
        if (col < 0 || col >= this.cols - 1) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Index out of range: Column {0} must be between 0 and {1}", col,
                            this.cols - 1));
        }

        double[] result = new double[this.rows];

        for (int i = 0; i < this.rows; i++) {
            result[i] = this.data[i][col];
        }

        return result;
    }

    public Matrix transpose() {
        double[][] newData = new double[this.cols][this.rows];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                newData[j][i] = this.data[i][j];
            }
        }

        return new Matrix(newData);
    }

    public Matrix plus(Matrix other) throws IllegalArgumentException {
        if (this.cols != other.cols || this.rows != other.rows) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                            this.cols, other.rows, other.cols));
        }

        double[][] newData = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                newData[i][j] = this.data[i][j] + other.data[i][j];
            }
        }

        return new Matrix(newData);
    }

    public Matrix minus(Matrix other) throws IllegalArgumentException {
        if (this.cols != other.cols || this.rows != other.rows) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                            this.cols, other.rows, other.cols));
        }

        double[][] data = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                data[i][j] = this.data[i][j] - other.data[i][j];
            }
        }

        return new Matrix(data);
    }

    public Matrix multiplyElementWise(Matrix other) throws IllegalArgumentException {
        if (this.cols != other.rows) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                            this.cols, other.rows, other.cols));
        }

        double[][] newData = new double[this.rows][other.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                newData[i][j] = this.data[i][j] * other.data[i][j];
            }
        }

        return new Matrix(newData);
    }

    public Matrix multiplyMatrixWise(Matrix other) throws IllegalArgumentException {
        if (this.cols != other.rows) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                            this.cols, other.rows, other.cols));
        }

        double[][] newData = new double[this.rows][other.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                double value = 0;

                for (int k = 0; k < this.cols; k++) {
                    value += this.data[i][k] * other.data[k][j];
                }

                newData[i][j] = value;
            }
        }

        return new Matrix(newData);
    }
}
package math;

public class Problem {
    private Matrix objectiveFunctionMultipliers;
    private Matrix constraintsMultipliers;
    private Matrix bounds;

    public Problem(Matrix objectiveFunctionMultipliers, Matrix constraintsMultipliers, Matrix bounds) {
        this.objectiveFunctionMultipliers = objectiveFunctionMultipliers;
        this.constraintsMultipliers = constraintsMultipliers;
        this.bounds = bounds;
    }

    public Problem clone() {
        return new Problem(
                this.objectiveFunctionMultipliers.clone(),
                this.constraintsMultipliers.clone(),
                this.bounds.clone());
    }

    public int getNumberOfConstraints() {
        return this.constraintsMultipliers.getNumberOfRows();
    }

    public int getNumberOfVariables() {
        return this.objectiveFunctionMultipliers.getNumberOfColumns();
    }

    public Matrix getObjectiveFunctionMultipliers() {
        return this.objectiveFunctionMultipliers;
    }

    public Matrix getConstraints() {
        return this.constraintsMultipliers;
    }

    public Matrix getBounds() {
        return this.bounds;
    }

    public Problem addConstraint(double[] constraintMultipliers, double bound) throws Exception {
        this.constraintsMultipliers = this.constraintsMultipliers.addRow(constraintMultipliers);
        this.bounds = this.bounds.addColumn(new double[] { bound });
        return this;
    }
}
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
package math;

import interfaces.Solver;
import util.Result;

public class TwoPhaseSolver implements Solver {
    @Override
    public Result run(Problem problem) {
        TwoPhaseSimplexSolver solver = new TwoPhaseSimplexSolver(problem.getConstraints().toRawMatrix(),
                problem.getBounds().toRawVector(), problem.getObjectiveFunctionMultipliers().toRawVector());
        return new Result(
                solver.primalSolution(),
                solver.optimalValue());
    }
}
package util;

public class Result {
    private double[] solution;
    private double objectiveValue;

    public Result(double[] solution, double objectiveValue) {
        this.solution = solution;
        this.objectiveValue = objectiveValue;
    }

    public double[] getSolution() {
        return this.solution;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }
}
