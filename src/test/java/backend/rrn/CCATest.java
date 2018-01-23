package backend.rrn;

import static org.ejml.EjmlUnitTests.*;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import static org.ejml.dense.row.CommonOps_DDRM.*;

import org.ejml.dense.row.decomposition.eig.symm.SymmetricQrAlgorithm_DDRM;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CCATest {

    private static final double TOL = 1e-5;

    public static String[][] read_csv(String filename) throws IOException {
        String thisLine;
        int count=0;
        FileInputStream fis = new FileInputStream(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        List<String[]> lines = new ArrayList<String[]>();
        while ((thisLine = reader.readLine()) != null) {
            lines.add(thisLine.split(","));
        }

        // convert our list to a String array.
        String[][] array = new String[lines.size()][0];
        lines.toArray(array);

        return array;
    }

    public static double[][] to_double_array(String[][] arr) {
        int rows = arr.length;
        int columns = arr[0].length;
        double[][] ret = new double[rows][columns];
        for (int i = 0; i != rows; ++i) {
            for (int j = 0; j != columns; ++j) {
                ret[i][j] = Double.parseDouble(arr[i][j]);
            }
        }
        return ret;
    }

    @Test
    public void read_test() throws IOException {

        Path folder = Paths.get("/home/artef/IdeaProjects/prrn-mosaic/python_scripts/test_files/");

        DMatrixRMaj expected_X = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_X.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_Y = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_Y.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_U = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_U.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_V = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_V.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_a = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_a.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_b = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_b.csv").normalize().toString() ) ) );

        DMatrixRMaj expected_x_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("x_sum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_xx_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xx_sum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_xy_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xy_sum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_yy_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("yy_sum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_y_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("y_sum.csv").normalize().toString() ) ) );

        DMatrixRMaj expected_xx_cov = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xx_cov.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_xy_cov = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xy_cov.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_yy_cov = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("yy_cov.csv").normalize().toString() ) ) );

        DMatrixRMaj expected_xx_cov_sqrt_inv = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xx_cov_sqrt_inv.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_yy_cov_sqrt_inv = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("yy_cov_sqrt_inv.csv").normalize().toString() ) ) );

        // Use Efficient Java Matrix Library for computations
        // Because it seems to prove it's robustness
        // See benchmarks for more info:
        // https://lessthanoptimal.github.io/Java-Matrix-Benchmark/runtime/2013_10_Corei7v2600/

        DMatrixRMaj X = new DMatrixRMaj(expected_X);
        DMatrixRMaj Y = new DMatrixRMaj(expected_Y);
        int n_bands = X.getNumCols();
        int n = X.getNumRows();

        CCA CCA_calc = new CCA(n_bands);

        // Push into CCA with 10-batches
        int push_batch = 10;

        CCA_calc.push(X,Y);

        assertEquals(expected_x_sum,CCA_calc.x_sum, TOL);
        assertEquals(expected_xx_sum,CCA_calc.xx_sum, TOL);
        assertEquals(expected_xy_sum,CCA_calc.xy_sum, TOL);
        assertEquals(expected_yy_sum,CCA_calc.yy_sum, TOL);
        assertEquals(expected_y_sum,CCA_calc.y_sum, TOL);

        CCA_calc.compute();

        assertEquals(expected_xx_cov,CCA_calc.xx_cov,TOL);
        assertEquals(expected_xy_cov,CCA_calc.xy_cov,TOL);
        assertEquals(expected_yy_cov,CCA_calc.yy_cov,TOL);
        assertEquals(expected_xx_cov_sqrt_inv,CCA_calc.xx_cov_sqrt_inv, TOL);
        assertEquals(expected_yy_cov_sqrt_inv,CCA_calc.yy_cov_sqrt_inv, TOL);

//        System.out.println(CCA_calc.a);
        System.out.println(CCA_calc.b);
//
//        DMatrixRMaj xx_cov = new DMatrixRMaj(n_bands,n_bands);
//        CCA_calc.calc_cov(CCA_calc.xx_sum,CCA_calc.x_sum,CCA_calc.x_sum,n,xx_cov);

//        System.out.println(xx_cov);
//        assertEquals(expected_xx_cov,xx_cov,TOL);

//
//        int n_bands = X.getNumCols();
//
//        DMatrixRMaj x_sum = new DMatrixRMaj(n_bands,1);
//
//        sumCols(X,x_sum);
//
//        EjmlUnitTests.assertEquals(expected_x_sum,x_sum, UtilEjml.TEST_F64);

//        DMatrixRMaj xx_cov_sqrtm = new DMatrixRMaj(n_bands,n_bands);
//        CCA_calc.sqrtm(expected_xx_cov,xx_cov_sqrtm);
//        System.out.println(xx_cov_sqrtm);

    }


}