package com.mihaibojescu.solvers.branch_and_bound.math;

import com.mihaibojescu.solvers.branch_and_bound.interfaces.Solver;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;

public class CuttingPlaneSolverAdapter implements Solver {
    @Override
    public Result run(Problem problem) throws Exception {
        CuttingPlaneSolver solver = new CuttingPlaneSolver(problem.getConstraints().toRawMatrix(),
                problem.getBounds().toRawVector(), problem.getObjectiveFunctionMultipliers().toRawVector());
        Matrix solution = new Matrix(solver.solve());

        return new Result(
                solution.toRawVector(),
                solution.transpose().dot(problem.getObjectiveFunctionMultipliers()).get(0, 0));
    }
}