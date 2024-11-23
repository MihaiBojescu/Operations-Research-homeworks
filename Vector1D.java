import java.text.MessageFormat;

public class Vector1D {
    protected float[] data;

    public Vector1D(int size) {
        this.data = new float[size];
    }

    public Vector1D(float[] data) {
        this.data = data;
    }

    public float get(int index) throws Exception {
        if (index < 0 || index >= this.data.length) {
            throw new Exception(
                    MessageFormat.format("Index out of range: {0} must be between 0 and {1}", index,
                            this.data.length - 1));
        }
        return this.data[index];
    }

    public void set(int index, float value) throws Exception {
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

        float[] resultData = new float[this.data.length];
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

        float[] resultData = new float[this.data.length];
        for (int i = 0; i < this.data.length; i++) {
            resultData[i] = this.data[i] - other.data[i];
        }

        return new Vector1D(resultData);
    }
}
