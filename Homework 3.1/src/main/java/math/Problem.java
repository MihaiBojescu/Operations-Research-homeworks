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
        if (this.constraintsMultipliers.doesRowExist(constraintMultipliers)
                && this.bounds.doesColumnExist(new double[] { bound })) {
            return this;
        }

        this.constraintsMultipliers = this.constraintsMultipliers.addRow(constraintMultipliers);
        this.bounds = this.bounds.addColumn(new double[] { bound });
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n");
        builder.append("max\t");

        for (int i = 0; i < this.objectiveFunctionMultipliers.getNumberOfColumns() - 1; i++) {
            builder.append(this.objectiveFunctionMultipliers.get(0, i));
            builder.append(" * x");
            builder.append(i + 1);
            builder.append(" + ");
        }

        builder.append(
                this.objectiveFunctionMultipliers.get(0, this.objectiveFunctionMultipliers.getNumberOfColumns() - 1));
        builder.append(" * x");
        builder.append(this.objectiveFunctionMultipliers.getNumberOfColumns());

        builder.append("\n");
        builder.append("s.t.\t");

        for (int i = 0; i < this.constraintsMultipliers.getNumberOfRows(); i++) {
            for (int j = 0; j < this.constraintsMultipliers.getNumberOfColumns() - 1; j++) {
                builder.append(this.constraintsMultipliers.get(i, j));
                builder.append(" * x");
                builder.append(j + 1);
                builder.append(" + ");
            }

            builder.append(
                    this.constraintsMultipliers.get(i,
                            this.constraintsMultipliers.getNumberOfColumns() - 1));
            builder.append(" * x");
            builder.append(this.constraintsMultipliers.getNumberOfColumns());

            builder.append(" <= ");
            builder.append(this.bounds.get(0, i));

            builder.append("\n");
            builder.append("\t");
        }

        return builder.toString();
    }
}
