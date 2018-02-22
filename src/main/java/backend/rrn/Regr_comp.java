package backend.rrn;

import org.ejml.data.DMatrixRMaj;

import static org.ejml.dense.row.CommonOps_DDRM.*;
import static org.ejml.dense.row.CommonOps_DDRM.insert;

/**
 * computition of regression
 */
public class Regr_comp {

    private int n_bands;

    // Buffer matrices for calculation purposes
    private DMatrixRMaj lin_reg_buf_1_b;
    private DMatrixRMaj lin_reg_buf_b_1;
    private DMatrixRMaj lin_reg_buf_b_b;
    private DMatrixRMaj m1;
    private DMatrixRMaj m2;


    public Regr_comp(int i_n_bands) {

        n_bands = i_n_bands;

        // Initialize buffer matrices
        lin_reg_buf_1_b = new DMatrixRMaj(1,n_bands);
        lin_reg_buf_b_1 = new DMatrixRMaj(n_bands,1);
        lin_reg_buf_b_b = new DMatrixRMaj(n_bands,n_bands);
        m1 = new DMatrixRMaj(2,2);
        m2 = new DMatrixRMaj(2,1);

    }

    /**
     * Calculate linear regression of U and V using X and Y statistics and their linear
     * combination coefficients a and b
     * where
     * U[i] = a[i]'X
     * V[i] = b[i]'Y
     * @param n - number of samples
     * @param a - X linear coefficients column-vector
     * @param b - Y linear coefficients column-vector
     * @param x_sum - band sum of X
     * @param y_sum - band sum of Y
     * @param xy_sum X Y covariance matrix
     * @param xx_sum X X covariance matrix
     * @param ret - Return matrix
     * @param ret_y row to put return value into
     * @param ret_x column to put return value into
     */
    public void linear_regression(int n, DMatrixRMaj a, DMatrixRMaj b, DMatrixRMaj x_sum, DMatrixRMaj y_sum,
                                         DMatrixRMaj xy_sum, DMatrixRMaj xx_sum, DMatrixRMaj ret, int ret_y, int ret_x) {


        // 0,0 item
        m1.set(0,0,n);

        // 0,1; 1,0 item
        elementMult(a,x_sum,lin_reg_buf_b_1);
        elementSum(lin_reg_buf_b_1);
        m1.set(0,1, elementSum(lin_reg_buf_b_1) );
        m1.set( 1,0, elementSum(lin_reg_buf_b_1) );

        // 1,1 item

        transpose(a,lin_reg_buf_1_b);
        mult(a,lin_reg_buf_1_b,lin_reg_buf_b_b);
        elementMult(lin_reg_buf_b_b,xx_sum);
        m1.set(1,1,elementSum(lin_reg_buf_b_b) );

        // Step2 - calculate m2 matrix

        // 0,0 item
        elementMult(b,y_sum,lin_reg_buf_b_1);
        m2.set(0,0,elementSum(lin_reg_buf_b_1));

        // 1,0 item
        transpose(b,lin_reg_buf_1_b);
        mult(a,lin_reg_buf_1_b,lin_reg_buf_b_b);
        elementMult(lin_reg_buf_b_b,xy_sum);
        m2.set( 1,0, elementSum(lin_reg_buf_b_b) );

        // Step 3 - calculate b and intercept

        invert(m1);

        DMatrixRMaj ret2 = new DMatrixRMaj(2,1);
        //DMatrixRMaj ret3 = new DMatrixRMaj()
        mult(m1,m2,ret2);

        // Insert numbers into provided return position
        transpose(ret2);
        insert(ret2,ret,ret_y,ret_x);
    }

    /**
     * Calculate linear regression for all bands
     * (uses {@link #linear_regression(int, DMatrixRMaj, DMatrixRMaj, DMatrixRMaj, DMatrixRMaj, DMatrixRMaj, DMatrixRMaj, DMatrixRMaj, int, int)}
     * for each band mulitple times
     * @param n - number of saples
     * @param a - a-coef matrix
     * @param b - b-coef matrix
     * @param x_sum - sum of X for each band
     * @param y_sum - sum of Y for each band
     * @param xy_sum - sum of x*y for each band
     * @param xx_sum - sum of x*x for each band
     * @param ret - output matrix
     */
    public void compute(int n, DMatrixRMaj a, DMatrixRMaj b, DMatrixRMaj x_sum, DMatrixRMaj y_sum,
                        DMatrixRMaj xy_sum, DMatrixRMaj xx_sum, DMatrixRMaj ret) {

        // Iterate for all bands
        for (int target_band = 0; target_band != n_bands; ++target_band) {
            linear_regression(
                    n,
                    extract(a, 0, n_bands, target_band, target_band+1), // 0-band of a
                    extract(b, 0, n_bands, target_band, target_band+1), // 0-band of b
                    x_sum,
                    y_sum,
                    xy_sum,
                    xx_sum,
                    ret,
                    target_band,
                    0
            );
        }
    }

}
