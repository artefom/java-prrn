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

class ArrayIndexComparatorEig implements Comparator<Integer>
{
    private final EigenDecomposition_F64 eig;

    public ArrayIndexComparatorEig(EigenDecomposition_F64 eig)
    {
        this.eig = eig;
    }

    public void createIndexArray(Integer[] indexes)
    {
        int len = eig.getNumberOfEigenvalues();
        for (int i = 0; i < len; i++)
        {
            indexes[i] = i; // Autoboxing
        }
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
        // Autounbox from Integer to int to use as array indexes
        return -Double.compare( eig.getEigenvalue(index1).real, eig.getEigenvalue(index2).real );
    }
}

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

    // SQRTM variables
    private DMatrixRMaj eigenvals_buf,eigenvecs_buf;
    private DMatrixRMaj sqrt_buf;
    private Integer[] index_array;

    // Decomposition engine
    private SymmetricQRAlgorithmDecomposition_DDRM decomp;

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

        // Matrix to hold eigenvalues
        eigenvals_buf = new DMatrixRMaj(n_bands,n_bands);
        // Matrix to hold eigenvectors
        eigenvecs_buf = new DMatrixRMaj(n_bands,n_bands);

        sqrt_buf = new DMatrixRMaj(n_bands,n_bands);

        index_array = new Integer[n_bands];

        decomp = new SymmetricQRAlgorithmDecomposition_DDRM(true);

        cov_buf1 = new DMatrixRMaj(1,n_bands);
        cov_buf2 = new DMatrixRMaj(n_bands,n_bands);
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
        calc_cov(xx_sum,x_sum,x_sum,n,xx_cov);
        calc_cov(xy_sum,x_sum,y_sum,n,xy_cov);
        calc_cov(yy_sum,y_sum,y_sum,n,yy_cov);

        // Compute inverse square of xx_cov
        n_bands_mat_buf.set(xx_cov);
        sqrtm(n_bands_mat_buf, xx_cov_sqrt_inv);
        invert(xx_cov_sqrt_inv);

        // Compute inverse square of yy_cov
        n_bands_mat_buf.set(yy_cov);
        sqrtm(n_bands_mat_buf, yy_cov_sqrt_inv);
        invert(yy_cov_sqrt_inv);

        transpose(xy_cov,yx_cov);

//u_mat = xx_cov_sqrt_inv @ xy_cov @ np.linalg.inv(yy_cov) @ xy_cov.T @ xx_cov_sqrt_inv
//u_eigvals,u_eigvecs = np.linalg.eig(u_mat)

        invert(yy_cov,n_bands_mat_buf);
        mult(xy_cov,n_bands_mat_buf,n_bands_mat_buf2);
        mult(xx_cov_sqrt_inv,n_bands_mat_buf2,n_bands_mat_buf);
        mult(n_bands_mat_buf,yx_cov,n_bands_mat_buf2);
        mult(n_bands_mat_buf2,xx_cov_sqrt_inv,n_bands_mat_buf);


        // Decompose matrix, to find eigenvectors and eigenvalues
        if (!decomp.decompose(n_bands_mat_buf)) {
            throw new IllegalArgumentException();
        }

        // Sort eigenvectors by corresponding eigenvalues
        ArrayIndexComparatorEig comparator = new ArrayIndexComparatorEig(decomp);
        comparator.createIndexArray(index_array);
        Arrays.sort(index_array,comparator);

        for (int to_column = 0; to_column != n_bands; ++to_column) {
            int from_column = index_array[to_column];
            DMatrixRMaj eigvec = decomp.getEigenVector(from_column);
            for (int row = 0; row != n_bands; ++row) {
                n_bands_mat_buf2.set(row,to_column,eigvec.get(row));
            }
        }
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

//        b.set(n_bands_mat_buf);

        // Decompose matrix, to find eigenvectors and eigenvalues
        if (!decomp.decompose(n_bands_mat_buf)) {
            throw new IllegalArgumentException();
        }

        // Sort eigenvectors by corresponding eigenvalues
        Arrays.sort(index_array,comparator);
        for (int to_column = 0; to_column != n_bands; ++to_column) {
            int from_column = index_array[to_column];
            DMatrixRMaj eigvec = decomp.getEigenVector(from_column);
            for (int row = 0; row != n_bands; ++row) {
                n_bands_mat_buf2.set(row,to_column,eigvec.get(row));
            }
        }

        transpose(n_bands_mat_buf2);
        mult(n_bands_mat_buf2,yy_cov_sqrt_inv,b);
        transpose(b);

    }

    // Regression function


//def calc_linear_regression(n,a,b,x_sum, y_sum, xy_sum, xx_sum ):
//    m1 = np.array([[n,(a @ x_sum)[0]],
//            [(a @ x_sum)[0],((a[:,np.newaxis] @ a[:,np.newaxis].T) * xx_sum).sum()]])
//    m2 = np.array([
//            (b @ y_sum)[0],
//            ( ( a[:,np.newaxis] @ b[np.newaxis,:] ) * ( xy_sum ) ).sum()
//    ])
//            return np.linalg.inv(m1) @ m2



    public static void linear_regression(int n, DMatrixRMaj a, DMatrixRMaj b, DMatrixRMaj x_sum, DMatrixRMaj y_sum,
                                         DMatrixRMaj xy_sum, DMatrixRMaj xx_sum, DMatrix2x2 ret) {

//    m1 = np.array([[n,(a @ x_sum)[0]],
//            [(a @ x_sum)[0],((a[:,np.newaxis] @ a[:,np.newaxis].T) * xx_sum).sum()]])
        DMatrix2x2 m1 = new DMatrix2x2();

//    m2 = np.array([
//            (b @ y_sum)[0],
//            ( ( a[:,np.newaxis] @ b[np.newaxis,:] ) * ( xy_sum ) ).sum()
        DMatrix2x2 m2 = new DMatrix2x2();

        DMatrix2x2 buf = new DMatrix2x2();

        // return np.linalg.inv(m1) @ m2
        CommonOps_DDF2.invert(m1,buf);
        CommonOps_DDF2.mult(buf,m2,ret);

    }

    // Local matrices to calc_cov

    DMatrixRMaj cov_buf1,cov_buf2;

    /**
     * Calculate covariance of x and y
     * @param xy_sum
     */
    public void calc_cov(DMatrixRMaj xy_sum, DMatrixRMaj x_sum, DMatrixRMaj y_sum, int n, DMatrixRMaj ret) {
        // ( xy_sum - np.matmul(x_sum,y_sum.T)/n )/(n-1)

        transpose(y_sum,cov_buf1);
        mult(x_sum,cov_buf1,cov_buf2);
        divide(cov_buf2,n,cov_buf2);
        subtract(xy_sum,cov_buf2,ret);
        divide(ret,(n-1),ret);
    }

    /**
     * Matrix square root
     * @param mat
     */
    public void sqrtm(DMatrixRMaj mat, DMatrixRMaj result) throws IllegalArgumentException {

        if (!decomp.decompose(mat)) {
            throw new IllegalArgumentException();
        }

//        ArrayIndexComparatorEig comparator = new ArrayIndexComparatorEig(decomp);
//        comparator.createIndexArray(index_array);
//        Arrays.sort(index_array,comparator);

        // Extract eigenvectors and eigenvalues to pre-allocated variables
        for (int i = 0; i != n_bands; ++i ) {
            eigenvals_buf.unsafe_set(i, i, Math.sqrt(decomp.getEigenvalue(i).real));
            decomp.getEigenVector(i);
            DMatrixRMaj eigvec = decomp.getEigenVector(i);
            for (int j = 0; j != n_bands; ++j) {
                eigenvecs_buf.set( j,i,eigvec.get(j) );
            }
        }

        mult(eigenvecs_buf,eigenvals_buf,sqrt_buf);
        transpose(eigenvecs_buf);
        mult(sqrt_buf,eigenvecs_buf,result);
    }


}
