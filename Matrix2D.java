import java.text.MessageFormat;

public class Matrix2D extends Vector1D {
    private int rows;
    private int cols;

    public Matrix2D(int rows, int cols) {
        super(new float[rows * cols]);

        this.rows = rows;
        this.cols = cols;
    }

    public Matrix2D(float[][] data) {
        super(new float[data.length * data[0].length]);
        this.rows = data.length;
        this.cols = data[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i * cols + j] = data[i][j];
            }
        }
    }

    public float get(int row, int col) throws Exception {
        if (row < 0 || row >= this.rows || col < 0 || col >= this.cols) {
            throw new Exception(
                    MessageFormat.format("Index out of range: ({0}, {1}) must be between (0, 0) and ({2}, {3})", row,
                            col, this.rows - 1, this.cols - 1));
        }

        return this.data[row * cols + col];
    }

    public void set(int row, int col, float value) throws Exception {
        if (row < 0 || row >= this.rows || col < 0 || col >= this.cols) {
            throw new Exception(
                    MessageFormat.format("Index out of range: ({0}, {1}) must be between (0, 0) and ({2}, {3})", row,
                            col, this.rows - 1, this.cols - 1));
        }

        this.data[row * cols + col] = value;
    }

    public Matrix2D transpose() {
        float[][] transposedData = new float[this.cols][this.rows];

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

        float[][] resultData = new float[this.rows][this.cols];

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

        float[][] resultData = new float[this.rows][this.cols];

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

        float[][] resultData = new float[this.rows][other.cols];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                float value = 0;

                for (int k = 0; k < this.cols; k++) {
                    value += this.data[i * cols + k] * other.data[k * other.cols + j];
                }

                resultData[i][j] = value;
            }
        }

        return new Matrix2D(resultData);
    }
}
