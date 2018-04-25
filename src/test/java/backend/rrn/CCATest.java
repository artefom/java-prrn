package backend.rrn;

import static org.ejml.EjmlUnitTests.*;

import org.ejml.data.DMatrixRMaj;
import static org.ejml.dense.row.CommonOps_DDRM.*;
import static backend.utils.MatUtils.*;

import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CCATest {

    private static final double TOL = 1e-4;

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
        DMatrixRMaj expected_regr_out = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_regr.csv").normalize().toString() ) ) );

        DMatrixRMaj expected_x_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("x_wsum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_xx_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xx_wsum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_xy_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("xy_wsum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_yy_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("yy_wsum.csv").normalize().toString() ) ) );
        DMatrixRMaj expected_y_sum = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("y_wsum.csv").normalize().toString() ) ) );

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
        DMatrixRMaj w = null;

        if (w == null) {
            w = new DMatrixRMaj(X.numRows,1);
            for (int row = 0; row != w.numRows; ++row) {
                w.set(row,0,1);
            }
        }

        int n_bands = X.getNumCols();
        int n = X.getNumRows();

        CCA CCA_calc = new CCA(n_bands);

        // Push into CCA with 10-batches
        int batch_size = 10;

        for (int batch_start = 0; batch_start < n; batch_start+=batch_size) {
            int batch_end = batch_start+batch_size;
            if (batch_end > n) batch_end = n;

            DMatrixRMaj X_batch = extract(X,batch_start,batch_end,0,n_bands);
            DMatrixRMaj Y_batch = extract(Y,batch_start,batch_end,0,n_bands);
            DMatrixRMaj w_batch = extract(w,batch_start,batch_end,0,1);
            CCA_calc.push(X_batch,Y_batch,w_batch);
        }

        assertEquals(expected_x_sum,CCA_calc.x_wsum, TOL);
        assertEquals(expected_xx_sum,CCA_calc.xx_wsum, TOL);
        assertEquals(expected_xy_sum,CCA_calc.xy_wsum, TOL);
        assertEquals(expected_yy_sum,CCA_calc.yy_wsum, TOL);
        assertEquals(expected_y_sum,CCA_calc.y_wsum, TOL);

        CCA_calc.compute();

        assertEquals(expected_xx_cov,CCA_calc.xx_cov,TOL);
        assertEquals(expected_xy_cov,CCA_calc.xy_cov,TOL);
        assertEquals(expected_yy_cov,CCA_calc.yy_cov,TOL);
        assertEquals(expected_xx_cov_sqrt_inv,CCA_calc.xx_cov_sqrt_inv, TOL);
        assertEquals(expected_yy_cov_sqrt_inv,CCA_calc.yy_cov_sqrt_inv, TOL);

        // Check that coefficient ratio match
        // Changes in sign may be due to different eigen-value decomposition algorithm.
        // And the magnitude of a and b does not really matter
        // only their ratio

        for (int row = 0; row != n_bands; ++row) {
            for (int column = 0; column != n_bands; ++column) {
                double expected_ratio = expected_a.get(row,column)/expected_b.get(row,column);
                double got_ratio = CCA_calc.a.get(row,column)/CCA_calc.b.get(row,column);
                assertEquals(expected_ratio,got_ratio,TOL);
            }
        }


    }

    @Test
    public void wsumColsTest() throws IOException {
        Path folder = Paths.get("/home/artef/IdeaProjects/prrn-mosaic/python_scripts/test_files/");

        DMatrixRMaj expected_X = new DMatrixRMaj( to_double_array( read_csv( folder.resolve("test_X.csv").normalize().toString() ) ) );
        DMatrixRMaj X = new DMatrixRMaj(expected_X);
        DMatrixRMaj w = null;
        if (w == null) {
            w = new DMatrixRMaj(X.numRows,1);
            for (int row = 0; row != w.numRows; ++row) {
                w.set(row,0,1.0/(row+1));
            }
        }

        DMatrixRMaj ret;
        System.out.println("Weighted sum method1:");
        ret = sumCols( multRow(X,w,null),null);
        System.out.println(ret);

        System.out.println("Weighted sum method2:");
        ret = wsumCols(X,w,null);
        System.out.println(ret);
    }


}