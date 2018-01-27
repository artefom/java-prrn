package backend.rrn;

import org.ejml.data.DMatrixRMaj;

import static org.ejml.dense.row.CommonOps_DDRM.*;
import static org.ejml.dense.row.CommonOps_DDRM.divide;

/**
 * Computition of covariance matrix
 */
public class Cov_comp {

    private DMatrixRMaj cov_buf1, cov_buf2;

    public Cov_comp(int n_bands) {
        cov_buf1 = new DMatrixRMaj(1,n_bands);
        cov_buf2 = new DMatrixRMaj(n_bands,n_bands);
    }

    public void calc_cov(DMatrixRMaj xy_sum, DMatrixRMaj x_sum, DMatrixRMaj y_sum, int n, DMatrixRMaj ret) {
        // ( xy_sum - np.matmul(x_sum,y_sum.T)/n )/(n-1)

        transpose(y_sum,cov_buf1);
        mult(x_sum,cov_buf1,cov_buf2);
        divide(cov_buf2,n,cov_buf2);
        subtract(xy_sum,cov_buf2,ret);
        divide(ret,(n-1),ret);
    }
}
