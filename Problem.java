public class Problem {
    private Matrix solution;
    private Matrix objectiveFunctionMultipliers;
    private Matrix constraintsMultipliers;
    private Matrix bounds;
    private Result objectiveFunctionValue;

    public Problem(Matrix objectiveFunctionMultipliers, Matrix constraintsMultipliers, Matrix bounds) {
        this.objectiveFunctionMultipliers = objectiveFunctionMultipliers;
        this.constraintsMultipliers = constraintsMultipliers;
        this.bounds = bounds;
        this.objectiveFunctionValue = this.run();
    }

    public Problem clone() {
        return new Problem(
                this.objectiveFunctionMultipliers.clone(),
                this.constraintsMultipliers.clone(),
                this.bounds.clone());
    }

    public Result run() {
        return new Result(
                this.solution.getRow(0),
                this.solution.transpose().multiplyMatrixWise(this.objectiveFunctionMultipliers).get(0, 0));
    }

    public Result getResult() {
        return this.objectiveFunctionValue;
    }

    public int getNumberOfConstraints() {
        return this.constraintsMultipliers.getNumberOfRows();
    }

    public int getNumberOfVariables() {
        return this.solution.getNumberOfColumns();
    }

    public Problem addConstraint(double[] constraintMultipliers, double bound) throws Exception {
        this.constraintsMultipliers.addRow(constraintMultipliers);
        this.bounds.addRow(new double[] { bound });
        return this;
    }
}
