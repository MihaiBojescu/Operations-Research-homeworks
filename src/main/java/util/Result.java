package src.main.java.util;

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
