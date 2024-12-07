package com.mihaibojescu.solvers.branch_and_bound.math;

import com.mihaibojescu.solvers.branch_and_bound.interfaces.Solver;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;

public class TwoPhaseSimplexSolverAdapter implements Solver {
    @Override
    public Result run(Problem problem) {
        TwoPhaseSimplexSolver solver = new TwoPhaseSimplexSolver(problem.getConstraints().toRawMatrix(),
                problem.getBounds().toRawVector(), problem.getObjectiveFunctionMultipliers().toRawVector());
        return new Result(
                solver.primalSolution(),
                solver.optimalValue());
    }
}
