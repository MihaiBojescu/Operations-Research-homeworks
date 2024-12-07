package src.main.java.interfaces;

import src.main.java.math.Problem;
import src.main.java.util.Result;

public interface Solver {
    public static final double INF = Double.POSITIVE_INFINITY;
    public Result run(Problem problem) throws Exception;
}
