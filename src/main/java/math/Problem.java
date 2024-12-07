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
        this.constraintsMultipliers.addRow(constraintMultipliers);
        this.bounds.addRow(new double[] { bound });
        return this;
    }
}
