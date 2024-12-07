package interfaces;

import math.Problem;
import util.Result;

public interface Solver {
    public static final double INF = Double.POSITIVE_INFINITY;
    public Result run(Problem problem) throws Exception;
}
