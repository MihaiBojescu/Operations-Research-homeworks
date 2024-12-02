import java.text.MessageFormat;

public class Vector1D {
    protected double[] data;

    public Vector1D(int size) {
        this.data = new double[size];
    }

    public Vector1D(double[] data) {
        this.data = data;
    }

    public Vector1D extend(int dimension) throws Exception {
        if (dimension < 0) {
            throw new Exception("Invalid extension");
        }

        int newDimensions = this.data.length + dimension;
        double[] newData = new double[newDimensions];

        System.arraycopy(this.data, 0, newData, 0, newDimensions);

        return new Vector1D(newData);
    }

    public Vector1D extend(double[] data) throws Exception {
        double[] newData = new double[this.data.length + data.length];

        System.arraycopy(this.data, 0, newData, 0, this.data.length);
        System.arraycopy(data, 0, newData, this.data.length, data.length);

        return new Vector1D(newData);
    }

    public double get(int index) throws Exception {
        if (index < 0 || index >= this.data.length) {
            throw new Exception(
                    MessageFormat.format("Index out of range: {0} must be between 0 and {1}", index,
                            this.data.length - 1));
        }

        return this.data[index];
    }

    public void set(int index, double value) throws Exception {
        if (index < 0 || index >= this.data.length) {
            throw new Exception(
                    MessageFormat.format("Index out of range: {0} must be between 0 and {1}", index,
                            this.data.length - 1));
        }

        this.data[index] = value;
    }

    public Vector1D plus(Vector1D other) throws Exception {
        if (this.data.length != other.data.length) {
            throw new Exception(
                    MessageFormat.format("Invalid vector sizes: {0}, {1}", this.data.length, other.data.length));
        }

        double[] resultData = new double[this.data.length];

        for (int i = 0; i < this.data.length; i++) {
            resultData[i] = this.data[i] + other.data[i];
        }

        return new Vector1D(resultData);
    }

    public Vector1D minus(Vector1D other) throws Exception {
        if (this.data.length != other.data.length) {
            throw new Exception(
                    MessageFormat.format("Invalid vector sizes: {0}, {1}", this.data.length, other.data.length));
        }

        double[] resultData = new double[this.data.length];

        for (int i = 0; i < this.data.length; i++) {
            resultData[i] = this.data[i] - other.data[i];
        }

        return new Vector1D(resultData);
    }
}
