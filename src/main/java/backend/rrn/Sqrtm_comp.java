package backend.rrn;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_DDRM;

import static org.ejml.dense.row.CommonOps_DDRM.mult;
import static org.ejml.dense.row.CommonOps_DDRM.transpose;

/**
 * Computiton of matrix square root (M^{-1/2})
 */
public class Sqrtm_comp {

    // Decomposition engine
    private SymmetricQRAlgorithmDecomposition_DDRM decomp;
    private int n_bands;

    private DMatrixRMaj eigenvals_buf,eigenvecs_buf;
    private DMatrixRMaj sqrt_buf;

    public Sqrtm_comp(int i_n_bands) {
        n_bands = i_n_bands;

        // Matrix to hold eigenvalues
        eigenvals_buf = new DMatrixRMaj(n_bands,n_bands);
        // Matrix to hold eigenvectors
        eigenvecs_buf = new DMatrixRMaj(n_bands,n_bands);
        eigenvecs_buf = new DMatrixRMaj(n_bands,n_bands);

        sqrt_buf = new DMatrixRMaj(n_bands,n_bands);
        decomp = new SymmetricQRAlgorithmDecomposition_DDRM(true);
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
