package backend.rrn;

import org.ejml.data.DMatrixRMaj;

import static org.ejml.dense.row.CommonOps_DDRM.*;
import static org.ejml.dense.row.CommonOps_DDRM.insert;

/**
 * computition of regression
 */
public class Regr_comp {

    private int n_bands;

    public Regr_comp(int i_n_bands) {
        n_bands = i_n_bands;
    }

    public static void linear_regression(int n, DMatrixRMaj a, DMatrixRMaj b, DMatrixRMaj x_sum, DMatrixRMaj y_sum,
                                         DMatrixRMaj xy_sum, DMatrixRMaj xx_sum, DMatrixRMaj ret, int ret_y, int ret_x) {

        // *testing* Step1 - put a into ret

        DMatrixRMaj lin_reg_buf_1_b = new DMatrixRMaj(1,a.numRows);
        DMatrixRMaj lin_reg_buf_b_1 = new DMatrixRMaj(a.numRows,1);
        DMatrixRMaj lin_reg_buf_b_b = new DMatrixRMaj(a.numRows,a.numRows);

        // Step1 - calculate m1
        DMatrixRMaj m1 = new DMatrixRMaj(2,2);
        DMatrixRMaj m2 = new DMatrixRMaj(2,1);

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

        transpose(ret2);
        insert(ret2,ret,ret_x,ret_y);
    }

}
