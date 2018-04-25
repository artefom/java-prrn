package backend.utils;

import org.ejml.data.DMatrixRMaj;

public class MatUtils {

    /**
     * Multiples each row of matrix by corresponding vector value
     * out[i_row] = mat[i_row] * vec[i_row]
     * @param mat Input matrix
     * @param vec column-vector to be multiplied by
     * @param output vector to write output
     */
    public static DMatrixRMaj multRow(DMatrixRMaj mat,DMatrixRMaj vec, DMatrixRMaj output) {

        if (output == null) {
            output = new DMatrixRMaj(mat.numRows,mat.numCols);
        } else if (output.numCols != mat.numCols) {
            throw new IllegalArgumentException("Output does not have enough columns to store the results");
        } else if (output.numRows != mat.numRows) {
            throw new IllegalArgumentException("Output does not have enough rows to store the results");
        }

        for (int row = 0; row != mat.numRows; ++row) {
            for (int column = 0; column != mat.numCols; ++column) {
                output.set(row,column,
                        mat.get(row,column)*vec.get(row)
                );
            }
        }

        return output;
    }

    /**
     * In-place version of {@link #multRow(DMatrixRMaj, DMatrixRMaj, DMatrixRMaj)}
     * @param mat Input matrix (overriden)
     * @param vec column-vector to be multiplied by
     */
    public static void multRow(DMatrixRMaj mat, DMatrixRMaj vec) {
        for (int row = 0; row != mat.numRows; ++row) {
            for (int column = 0; column != mat.numCols; ++column) {
                mat.set(row,column,
                        mat.get(row,column)*vec.get(row)
                );
            }
        }
    }

    /**
     * Weighted sum of columns
     * @param input input matrix
     * @param weights weught column-vector or row-vector
     * @param output output matrix
     * @return output
     */
    public static DMatrixRMaj wsumCols(DMatrixRMaj input, DMatrixRMaj weights, DMatrixRMaj output) {
        if (output == null) {
            output = new DMatrixRMaj(1, input.numCols);
        } else if (output.getNumElements() != input.numCols) {
            throw new IllegalArgumentException("Output does not have enough elements to store the results");
        }

        for(int cols = 0; cols < input.numCols; ++cols) {
            double total = 0.0D;
            int index = cols;
            int row = 0;

            for(int end = cols + input.numCols * input.numRows; index < end; index += input.numCols, row+=1) {
                total += input.data[index]*weights.data[row];
            }

            output.set(cols, total);
        }

        return output;
    }
}
