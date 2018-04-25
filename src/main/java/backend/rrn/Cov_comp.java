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

    public void calc_cov(DMatrixRMaj xy_wsum, DMatrixRMaj x_wsum, DMatrixRMaj y_wsum, double w_sum, DMatrixRMaj ret) {
        // ( xy_wsum - (x_wsum @ y_wsum.T)/w_sum )/(w_sum-1)

        transpose(y_wsum,cov_buf1);
        mult(x_wsum,cov_buf1,cov_buf2);
        divide(cov_buf2,w_sum,cov_buf2);
        subtract(xy_wsum,cov_buf2,ret);
        divide(ret,(w_sum-1),ret);
    }
}
