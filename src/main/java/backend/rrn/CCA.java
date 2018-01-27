package backend.rrn;

import org.ejml.data.DMatrix2x2;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.fixed.CommonOps_DDF2;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static org.ejml.dense.row.EigenOps_DDRM.*;

import java.util.Arrays;
import java.util.Comparator;

import static org.ejml.dense.row.CommonOps_DDRM.*;

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
    public int n;
    public int n_bands;
    public DMatrixRMaj x_sum,xx_sum,xy_sum,yy_sum,y_sum;
    public DMatrixRMaj xx_cov,xy_cov, yx_cov, yy_cov;
    public DMatrixRMaj xx_cov_sqrt_inv, yy_cov_sqrt_inv;
    public DMatrixRMaj a,b;

    // Declared with computation speedup in mind
    private DMatrixRMaj n_bands_vec_buf,n_bands_mat_buf,n_bands_mat_buf2;

    // Computition engines
    private Cov_comp cov_comp;
    private Sqrtm_comp sqrtm_comp;
    private Regr_comp regr_comp;
    private Sorted_eig_comp sorted_eig_comp;


    /**
     * Setup, initialise matrices, allocate memory
     */
    public CCA(int i_n_bands) {
        this.n_bands = i_n_bands;

        x_sum = new DMatrixRMaj(n_bands,1);
        xx_sum = new DMatrixRMaj(n_bands,n_bands);
        xy_sum = new DMatrixRMaj(n_bands,n_bands);
        yy_sum = new DMatrixRMaj(n_bands,n_bands);
        y_sum = new DMatrixRMaj(n_bands,1);

        xx_cov = new DMatrixRMaj(n_bands,n_bands);
        xy_cov = new DMatrixRMaj(n_bands,n_bands);
        yx_cov = new DMatrixRMaj(n_bands,n_bands);
        yy_cov = new DMatrixRMaj(n_bands,n_bands);

        xx_cov_sqrt_inv = new DMatrixRMaj(n_bands,n_bands);
        yy_cov_sqrt_inv = new DMatrixRMaj(n_bands,n_bands);

        a = new DMatrixRMaj(n_bands,n_bands);
        b = new DMatrixRMaj(n_bands,n_bands);

        n_bands_vec_buf = new DMatrixRMaj(n_bands,1);
        n_bands_mat_buf = new DMatrixRMaj(n_bands, n_bands);
        n_bands_mat_buf2 = new DMatrixRMaj(n_bands,n_bands);

        cov_comp = new Cov_comp(n_bands);
        sqrtm_comp = new Sqrtm_comp(n_bands);
        regr_comp = new Regr_comp(n_bands);
        sorted_eig_comp = new Sorted_eig_comp(n_bands);
    }

    /**
     * warp around {@link #push(DMatrixRMaj,DMatrixRMaj)}
     * @param i_X x data
     * @param i_Y y data
     */
    public void push(double[][] i_X, double[][] i_Y) {
        push(new DMatrixRMaj(i_X),new DMatrixRMaj(i_Y));
    }

    /**
     * warp around {@link #pull(DMatrixRMaj, DMatrixRMaj)}
     * @param i_X x data
     * @param i_Y y data
     */
    public void pull(double[][] i_X, double[][] i_Y) {
        pull(new DMatrixRMaj(i_X),new DMatrixRMaj(i_Y));
    }

    public void push(DMatrixRMaj X, DMatrixRMaj Y) {

        //self.x_sum += np.sum(x,axis=0)[:,np.newaxis]
        sumCols(X,n_bands_vec_buf);
        add(n_bands_vec_buf,x_sum,x_sum);

        //self.y_sum += np.sum(y,axis=0)[:,np.newaxis]
        sumCols(Y,n_bands_vec_buf);
        add(n_bands_vec_buf,y_sum,y_sum);

        // Calculate transpositions
        DMatrixRMaj X_T = new DMatrixRMaj(X.numCols,X.numRows);
        DMatrixRMaj Y_T = new DMatrixRMaj(Y.numCols,Y.numRows);
        transpose(X,X_T);
        transpose(Y,Y_T);

        //self.xy_sum += np.matmul(np.transpose(x),y)
        mult(X_T,Y,n_bands_mat_buf);
        add(n_bands_mat_buf,xy_sum,xy_sum);

        //self.xx_sum += np.matmul(np.transpose(x),x)
        mult(X_T,X,n_bands_mat_buf);
        add(n_bands_mat_buf,xx_sum,xx_sum);

        //self.yy_sum += np.matmul(np.transpose(y),y)
        mult(Y_T,Y,n_bands_mat_buf);
        add(n_bands_mat_buf,yy_sum,yy_sum);

        //self.n += np.shape(x)[0]
        n+=X.numRows;
    }

    public void pull(DMatrixRMaj i_X, DMatrixRMaj i_Y) {
        throw new NotImplementedException();
    }


    // Compute variables

    public void compute() {
        cov_comp.calc_cov(xx_sum,x_sum,x_sum,n,xx_cov);
        cov_comp.calc_cov(xy_sum,x_sum,y_sum,n,xy_cov);
        cov_comp.calc_cov(yy_sum,y_sum,y_sum,n,yy_cov);

        // Compute inverse square of xx_cov
        n_bands_mat_buf.set(xx_cov);
        sqrtm_comp.sqrtm(n_bands_mat_buf, xx_cov_sqrt_inv);
        invert(xx_cov_sqrt_inv);

        // Compute inverse square of yy_cov
        n_bands_mat_buf.set(yy_cov);
        sqrtm_comp.sqrtm(n_bands_mat_buf, yy_cov_sqrt_inv);
        invert(yy_cov_sqrt_inv);

        transpose(xy_cov,yx_cov);

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

    }


}
