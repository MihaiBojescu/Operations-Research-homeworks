import java.text.MessageFormat;

public class Matrix2D extends Vector1D {
    private int rows;
    private int cols;

    public Matrix2D(int rows, int cols) {
        super(new double[rows * cols]);

        this.rows = rows;
        this.cols = cols;
    }

    public Matrix2D(double[] data) {
        super(data);
    }

    public Matrix2D(double[][] data) {
        super(new double[data.length * data[0].length]);
        this.rows = data.length;
        this.cols = data[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i * cols + j] = data[i][j];
            }
        }
    }

    public Matrix2D extendRows(int dimensions) throws Exception {
        if (dimensions < 0) {
            throw new Exception("Number of additional rows must be non-negative");
        }

        int newRows = this.rows + dimensions;
        double[] newData = new double[newRows * cols];

        System.arraycopy(data, 0, newData, 0, data.length);

        return new Matrix2D(newData);
    }

    public Matrix2D extendColumns(int dimensions) throws Exception {
        if (dimensions < 0) {
            throw new Exception("Number of additional columns must be non-negative");
        }

        int newCols = this.cols + dimensions;
        double[] newData = new double[this.rows * newCols];

        for (int r = 0; r < rows; r++) {
            System.arraycopy(data, r * this.cols, newData, r * newCols, this.cols);
        }

        return new Matrix2D(newData);
    }

    public Matrix2D extend(double[] additionalData, boolean extendByRow) {
        if (extendByRow) {
            int additionalRows = additionalData.length / cols;

            if (additionalData.length % cols != 0) {
                throw new IllegalArgumentException("Additional data does not match column count for row extension");
            }

            int newRows = rows + additionalRows;
            double[] newData = new double[newRows * cols];

            System.arraycopy(data, 0, newData, 0, data.length);
            System.arraycopy(additionalData, 0, newData, data.length, additionalData.length);

            return new Matrix2D(newData);
        } else {
            int additionalCols = additionalData.length / rows;

            if (additionalData.length % rows != 0) {
                throw new IllegalArgumentException("Additional data does not match row count for column extension");
            }

            int newCols = cols + additionalCols;
            double[] newData = new double[rows * newCols];

            for (int r = 0; r < rows; r++) {
                System.arraycopy(data, r * cols, newData, r * newCols, cols);
                System.arraycopy(additionalData, r * additionalCols, newData, r * newCols + cols, additionalCols);
            }

            return new Matrix2D(newData);
        }
    }

    public double get(int row, int col) throws Exception {
        if (row < 0 || row >= this.rows || col < 0 || col >= this.cols) {
            throw new Exception(
                    MessageFormat.format("Index out of range: ({0}, {1}) must be between (0, 0) and ({2}, {3})", row,
                            col, this.rows - 1, this.cols - 1));
        }

        return this.data[row * cols + col];
    }

    public void set(int row, int col, double value) throws Exception {
        if (row < 0 || row >= this.rows || col < 0 || col >= this.cols) {
            throw new Exception(
                    MessageFormat.format("Index out of range: ({0}, {1}) must be between (0, 0) and ({2}, {3})", row,
                            col, this.rows - 1, this.cols - 1));
        }

        this.data[row * cols + col] = value;
    }

    public Matrix2D transpose() {
        double[][] transposedData = new double[this.cols][this.rows];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                transposedData[j][i] = this.data[i * cols + j];
            }
        }

        return new Matrix2D(transposedData);
    }

    public Matrix2D plus(Matrix2D other) throws Exception {
        if (this.cols != other.cols || this.rows != other.rows) {
            throw new Exception(MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                    this.cols, other.rows, other.cols));
        }

        double[][] resultData = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                resultData[i][j] = this.data[i * cols + j] + other.data[i * cols + j];
            }
        }

        return new Matrix2D(resultData);
    }

    public Matrix2D minus(Matrix2D other) throws Exception {
        if (this.cols != other.cols || this.rows != other.rows) {
            throw new Exception(MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                    this.cols, other.rows, other.cols));
        }

        double[][] resultData = new double[this.rows][this.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                resultData[i][j] = this.data[i * cols + j] - other.data[i * cols + j];
            }
        }

        return new Matrix2D(resultData);
    }

    public Matrix2D dot(Matrix2D other) throws Exception {
        if (this.cols != other.rows) {
            throw new Exception(MessageFormat.format("Invalid matrix sizes: ({0}, {1}), ({2}, {3})", this.rows,
                    this.cols, other.rows, other.cols));
        }

        double[][] resultData = new double[this.rows][other.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                double value = 0;

                for (int k = 0; k < this.cols; k++) {
                    value += this.data[i * cols + k] * other.data[k * other.cols + j];
                }

                resultData[i][j] = value;
            }
        }

        return new Matrix2D(resultData);
    }
}
