package backend.rrn;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.decomposition.eig.SymmetricQRAlgorithmDecomposition_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

import java.util.Arrays;
import java.util.Comparator;

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

/**
 * Algorithm for sorted eigenvalues computation
 *
 * Performs an eigenvalue decomposition and returns eigenvectors sorted by eigenvalues
 */
public class Sorted_eig_comp {

    private SymmetricQRAlgorithmDecomposition_DDRM decomp;

    private Integer[] index_array;
    private int n_bands;
    private ArrayIndexComparatorEig comparator;

    public Sorted_eig_comp(int i_n_bands) {

        n_bands = i_n_bands;

        decomp = new SymmetricQRAlgorithmDecomposition_DDRM(true);
        comparator = new ArrayIndexComparatorEig(decomp);

        index_array = new Integer[n_bands];
    }


    /**
     * Decompose mat
     * @param mat - matrix to perform eigenvalue decomposition
     * @param out_eigvecs - vector of corresponding eigenvectors
     */
    public void decompose(DMatrixRMaj mat, DMatrixRMaj out_eigvecs) {
        // Decompose matrix, to find eigenvectors and eigenvalues
        if (!decomp.decompose(mat)) {
            throw new IllegalArgumentException();
        }

        // sort indexes of eigenvalues
        comparator.createIndexArray(index_array);
        Arrays.sort(index_array,comparator);

        // Fill output matrices based on index arrays
        for (int to_column = 0; to_column != n_bands; ++to_column) {
            int from_column = index_array[to_column];
            DMatrixRMaj eigvec = decomp.getEigenVector(from_column);
            //double eigval = decomp.getEigenvalue(from_column).real;
            //out_eigvals.set(to_column,eigval);
            for (int row = 0; row != n_bands; ++row) {
                out_eigvecs.set(row,to_column,eigvec.get(row));
            }
        }
    }
}
