package backend.rrn;

import org.ejml.data.DMatrixRMaj;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static backend.utils.MatUtils.*;
import static org.ejml.dense.row.CommonOps_DDRM.*;
// custom math functions


//class ArrayIndexComparatorEig implements Comparator<Integer>
//{
//    private final EigenDecomposition_F64 eig;
//
//    public ArrayIndexComparatorEig(EigenDecomposition_F64 eig)
//    {
//        this.eig = eig;
//    }
//
//    public void createIndexArray(Integer[] indexes)
//    {
//        int len = eig.getNumberOfEigenvalues();
//        for (int i = 0; i < len; i++)
//        {
//            indexes[i] = i; // Autoboxing
//        }
//    }
//
//    @Override
//    public int compare(Integer index1, Integer index2)
//    {
//        // Autounbox from Integer to int to use as array indexes
//        return -Double.compare( eig.getEigenvalue(index1).real, eig.getEigenvalue(index2).real );
//    }
//}

public class CCA {

    // Usefull statistics that can be used by external users
    public double w_sum;

    public int n_bands;
    public DMatrixRMaj x_wsum, xx_wsum, xy_wsum, yy_wsum, y_wsum;
    public DMatrixRMaj xx_cov,xy_cov, yx_cov, yy_cov;
    public DMatrixRMaj xx_cov_sqrt_inv, yy_cov_sqrt_inv;
    public DMatrixRMaj a,b; // linear transformation coef matrices
    public DMatrixRMaj regr_ret; // output of linear regression


    // Declared with computation speedup in mind
    private DMatrixRMaj n_bands_vec_buf,n_bands_mat_buf,n_bands_mat_buf2;

    // Computition engines
    private Cov_comp cov_comp; // Covariance computation
    private Sqrtm_comp sqrtm_comp; // Square root matrix computation
    private Regr_comp regr_comp; // Linear regression computation
    private Sorted_eig_comp sorted_eig_comp; // Eigen decomposition computation


    /**
     * Setup, initialise matrices, allocate memory
     */
    public CCA(int i_n_bands) {
        this.n_bands = i_n_bands;

        x_wsum = new DMatrixRMaj(n_bands,1);
        xx_wsum = new DMatrixRMaj(n_bands,n_bands);
        xy_wsum = new DMatrixRMaj(n_bands,n_bands);
        yy_wsum = new DMatrixRMaj(n_bands,n_bands);
        y_wsum = new DMatrixRMaj(n_bands,1);

        xx_cov = new DMatrixRMaj(n_bands,n_bands);
        xy_cov = new DMatrixRMaj(n_bands,n_bands);
        yx_cov = new DMatrixRMaj(n_bands,n_bands);
        yy_cov = new DMatrixRMaj(n_bands,n_bands);

        xx_cov_sqrt_inv = new DMatrixRMaj(n_bands,n_bands);
        yy_cov_sqrt_inv = new DMatrixRMaj(n_bands,n_bands);

        a = new DMatrixRMaj(n_bands,n_bands);
        b = new DMatrixRMaj(n_bands,n_bands);
        regr_ret = new DMatrixRMaj(n_bands,2);


        n_bands_vec_buf = new DMatrixRMaj(n_bands,1);
        n_bands_mat_buf = new DMatrixRMaj(n_bands, n_bands);
        n_bands_mat_buf2 = new DMatrixRMaj(n_bands,n_bands);

        cov_comp = new Cov_comp(n_bands);
        sqrtm_comp = new Sqrtm_comp(n_bands);
        regr_comp = new Regr_comp(n_bands);
        sorted_eig_comp = new Sorted_eig_comp(n_bands);
    }

//    /**
//     * warp around {@link #push(DMatrixRMaj,DMatrixRMaj, DMatrixRMaj)}
//     * @param i_X x data
//     * @param i_Y y data
//     */
//    public void push(double[][] i_X, double[][] i_Y) {
//        push(new DMatrixRMaj(i_X),new DMatrixRMaj(i_Y));
//    }
//
//    /**
//     * warp around {@link #pull(DMatrixRMaj, DMatrixRMaj)}
//     * @param i_X x data
//     * @param i_Y y data
//     */
//    public void pull(double[][] i_X, double[][] i_Y) {
//        pull(new DMatrixRMaj(i_X),new DMatrixRMaj(i_Y));
//    }

    public void push(DMatrixRMaj X, DMatrixRMaj Y, DMatrixRMaj w) {

        //self.xy_wsum += np.transpose(x) @ ( y*w[:,np.newaxis] )
        //self.xx_wsum += np.transpose(x) @ ( x*w[:,np.newaxis] )
        //self.yy_wsum += np.transpose(y) @ ( y*w[:,np.newaxis] )

        //self.w_sum += w.sum()

        // x_wsum  += np.sum(x*w[:,np.newaxis],axis=0)[:,np.newaxis]
        wsumCols(X,w,n_bands_vec_buf);
        add(n_bands_vec_buf, x_wsum, x_wsum);

        // y_wsum  += np.sum(y*w[:,np.newaxis],axis=0)[:,np.newaxis]
        wsumCols(Y,w,n_bands_vec_buf);
        add(n_bands_vec_buf, y_wsum, y_wsum);

        // Calculate transpositions
        DMatrixRMaj X_T = new DMatrixRMaj(X.numCols,X.numRows);
        DMatrixRMaj Y_T = new DMatrixRMaj(Y.numCols,Y.numRows);
        transpose(X,X_T);
        transpose(Y,Y_T);

        //self.xy_wsum += np.matmul(np.transpose(x),y)
        mult(X_T,Y,n_bands_mat_buf);
        add(n_bands_mat_buf, xy_wsum, xy_wsum);

        //self.xx_wsum += np.matmul(np.transpose(x),x)
        mult(X_T,X,n_bands_mat_buf);
        add(n_bands_mat_buf, xx_wsum, xx_wsum);

        //self.yy_wsum += np.matmul(np.transpose(y),y)
        mult(Y_T,Y,n_bands_mat_buf);
        add(n_bands_mat_buf, yy_wsum, yy_wsum);

        //self.w_sum += np.shape(x)[0]
        w_sum += elementSum(w);
    }

    public void pull(DMatrixRMaj i_X, DMatrixRMaj i_Y) {
        throw new NotImplementedException();
    }


    // Compute variables

    public void compute() {
        cov_comp.calc_cov(xx_wsum, x_wsum, x_wsum, w_sum,xx_cov);
        cov_comp.calc_cov(xy_wsum, x_wsum, y_wsum, w_sum,xy_cov);
        cov_comp.calc_cov(yy_wsum, y_wsum, y_wsum, w_sum,yy_cov);

        // Compute inverse square of xx_cov
        n_bands_mat_buf.set(xx_cov);
        sqrtm_comp.sqrtm(n_bands_mat_buf, xx_cov_sqrt_inv);
        invert(xx_cov_sqrt_inv);

        // Compute inverse square of yy_cov
        n_bands_mat_buf.set(yy_cov);
        sqrtm_comp.sqrtm(n_bands_mat_buf, yy_cov_sqrt_inv);
        invert(yy_cov_sqrt_inv);

        transpose(xy_cov,yx_cov);

        //Equivalent python code:
        //u_mat = xx_cov_sqrt_inv @ xy_cov @ np.linalg.inv(yy_cov) @ xy_cov.T @ xx_cov_sqrt_inv
        //u_eigvals,u_eigvecs = np.linalg.eig(u_mat)

        invert(yy_cov,n_bands_mat_buf);
        mult(xy_cov,n_bands_mat_buf,n_bands_mat_buf2);
        mult(xx_cov_sqrt_inv,n_bands_mat_buf2,n_bands_mat_buf);
        mult(n_bands_mat_buf,yx_cov,n_bands_mat_buf2);
        mult(n_bands_mat_buf2,xx_cov_sqrt_inv,n_bands_mat_buf);

        // Perform eigenvalue decomposition
        sorted_eig_comp.decompose(n_bands_mat_buf,n_bands_mat_buf2);
        // n_bands_mat_buf2 contains eigenvectors sorted by corresponding eigenvalues

        transpose(n_bands_mat_buf2);
        // calculate a
        mult(n_bands_mat_buf2,xx_cov_sqrt_inv,a);
        transpose(a);

        // Equivalent python code:
        //v_mat = yy_cov_sqrt_inv @ xy_cov.T @ np.linalg.inv(xx_cov) @ xy_cov @ yy_cov_sqrt_inv
        //v_eigvals,v_eigvecs = np.linalg.eig(v_mat)

        invert(xx_cov,n_bands_mat_buf);
        mult(yx_cov,n_bands_mat_buf,n_bands_mat_buf2);
        mult(yy_cov_sqrt_inv,n_bands_mat_buf2,n_bands_mat_buf);
        mult(n_bands_mat_buf,xy_cov,n_bands_mat_buf2);
        mult(n_bands_mat_buf2,yy_cov_sqrt_inv,n_bands_mat_buf);

        // Decompose matrix, to find eigenvectors and eigenvalues
        // Perform eigenvalue decomposition
        sorted_eig_comp.decompose(n_bands_mat_buf,n_bands_mat_buf2);
        // n_bands_mat_buf2 contains eigenvectors sorted by corresponding eigenvalues

        transpose(n_bands_mat_buf2);
        mult(n_bands_mat_buf2,yy_cov_sqrt_inv,b);
        transpose(b);

        // Compute linear regression for a
        regr_comp.compute(w_sum, a, b, x_wsum, y_wsum, xy_wsum, xx_wsum, regr_ret );

        // multiply a by regression coefficients.
        // By doing this, we also multiply U canonical variates by same coefs
        // If further we add intercept to U they'll become best-fitted to V variates
        // That will not change their correlation coefficients, but fit their magnitudes
        // closer. There is a practical reason for doing this.

        // i-th column of a is coefficient column-vector for i-th canonical variate
        for (int cur_band = 0; cur_band != n_bands; ++cur_band) {

            double mult_coef = regr_ret.get(cur_band,1);
            // To multiply i-th column vector by mult_coef we should:
            // Iterate through column and multiply each value by regression coefficient
            for (int coef_i = 0; coef_i != n_bands; ++coef_i ) {
                a.set(coef_i, cur_band,
                        a.get(coef_i,cur_band)*mult_coef
                );
            }
        }

    }


}
