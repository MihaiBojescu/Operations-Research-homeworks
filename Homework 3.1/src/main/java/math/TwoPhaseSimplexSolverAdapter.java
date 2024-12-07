package math;

import interfaces.Solver;
import util.Result;

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
