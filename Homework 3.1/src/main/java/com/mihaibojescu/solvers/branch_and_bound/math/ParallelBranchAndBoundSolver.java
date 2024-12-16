package com.mihaibojescu.solvers.branch_and_bound.math;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.mihaibojescu.solvers.branch_and_bound.interfaces.Solver;
import com.mihaibojescu.solvers.branch_and_bound.util.Box;
import com.mihaibojescu.solvers.branch_and_bound.util.Result;

public class ParallelBranchAndBoundSolver implements Solver {
    private Solver solver;
    private double tolerance;
    private int nprocs;
    private boolean debug;

    public ParallelBranchAndBoundSolver(Solver solver,
            double tolerance,
            int nprocs)
            throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        if (nprocs < 0) {
            throw new IllegalArgumentException(String.format("NProcs must be >= 0, but is %d", nprocs));
        }

        this.solver = solver;
        this.tolerance = tolerance;
        this.nprocs = nprocs;
        this.debug = false;
    }

    public ParallelBranchAndBoundSolver(Solver solver,
            double tolerance,
            int nprocs,
            boolean debug)
            throws IllegalArgumentException {
        if (tolerance < 0) {
            throw new IllegalArgumentException(String.format("Tolerance must be >= 0, but is %d", tolerance));
        }

        if (nprocs < 0) {
            throw new IllegalArgumentException(String.format("NProcs must be >= 0, but is %d", nprocs));
        }

        this.solver = solver;
        this.tolerance = tolerance;
        this.nprocs = nprocs;
        this.debug = debug;
    }

    @Override
    public Result run(Problem problem) throws Exception {
        BlockingQueue<Problem> input = new LinkedBlockingQueue<>();
        BlockingQueue<Problem> output = new LinkedBlockingQueue<>();
        ConcurrentHashMap<String, Boolean> visited = new ConcurrentHashMap<>();
        Box<Result> bestResult = new Box<>(new Result(null, -Solver.INF));

        ExecutorService executor = Executors.newFixedThreadPool(this.nprocs);

        input.offer(problem);

        while (!input.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(this.nprocs);

            for (int i = 0; i < this.nprocs; i++) {
                executor.submit(new ParallelBranchAndBoundSolverRunnable(
                        this.solver,
                        this.tolerance,
                        latch,
                        input,
                        output,
                        visited,
                        bestResult,
                        this.debug));
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.log("Interrupted while waiting for tasks to complete");
            }

            while (!output.isEmpty()) {
                input.offer(output.poll());
            }
        }

        return bestResult.getValue();
    }

    private void log(String string) {
        if (!this.debug) {
            return;
        }

        System.out.println(string);
    }
}
