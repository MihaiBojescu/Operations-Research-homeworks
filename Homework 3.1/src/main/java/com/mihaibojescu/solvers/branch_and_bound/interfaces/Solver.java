package com.mihaibojescu.solvers.branch_and_bound.interfaces;

import com.mihaibojescu.solvers.branch_and_bound.math.Problem;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;

public interface Solver {
    public static final double INF = Double.POSITIVE_INFINITY;
    public Result run(Problem problem) throws Exception;
}
