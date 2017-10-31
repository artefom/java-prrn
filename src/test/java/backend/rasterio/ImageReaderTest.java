package backend.rasterio;

import backend.utils.BufferUtils;
import backend.utils.TypeUtils;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Test class for ImageReader
 */
public class ImageReaderTest {

    RasterDataset ds1;
    RasterDataset ds2;
    private static final double DELTA = 1e-15;

    public ImageReaderTest() throws IOException {
        //Initialize gdal!
        gdal.AllRegister();

        String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
        String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

        ds1 = RasterDataset.from_file(path1);
        ds2 = RasterDataset.from_file(path2);
    }

    /**
     * Test for single tile
     */
    @Test
    public void test_read() {
        ImageReader reader = ImageReader.from_datasets(ds1,ds2);
        reader.set_window_size(1000,1000);
        reader.init();

        BlockInfo info = reader.read_block(0,0);

        int expected_width = 700;
        int expected_height = 930;

        assertEquals(expected_width,info.get_block_width());
        assertEquals(expected_height,info.get_block_height());

        // Make sure we have read 2 files
        assertEquals(info.data.size(),2);
        assertEquals(info.datasets.size(), 2);

        int[] x_block_size = new int[1];
        int[] y_block_size = new int[1];
        info.datasets.get(0).GetRasterBand(1).GetBlockSize(x_block_size,y_block_size);

//        System.out.println("Block x size: "+x_block_size[0]);
//        System.out.println("Block y size: "+y_block_size[0]);

        // Make sure we have read all 12 bands
        assertEquals(info.data.get(0).length,12);
        assertEquals(info.data.get(1).length,12);

        // Make sure number of pixels is correct

        for (int j = 0; j != 12; ++j) {
            int data_type = info.datasets.get(0).GetRasterBand(j+1).getDataType();
            int pixel_num = info.data.get(0)[j].remaining()/ TypeUtils.get_size(data_type);
            assertEquals(expected_width * expected_height, pixel_num);
        }
        // Now, make sure that pixel values are valid by calculating statistics

        // Make sure statistics match for area read

        //Dataset 1,  Band 1
        int data_type = info.datasets.get(0).GetRasterBand(1).getDataType();
        double[] data = BufferUtils.toDoubleArr( info.data.get(0)[0], data_type );
        assertEquals( 8513.5624178187409, calc_mean( data ), 1e-8 );
        assertEquals( 576.95433514124056, calc_std( data ), 1e-8 );

        //Dataset 2,  Band 1
        data_type = info.datasets.get(0).GetRasterBand(1).getDataType();
        data = BufferUtils.toDoubleArr( info.data.get(1)[0], data_type );
        assertEquals( 9949.9581536098303, calc_mean( data ), 1e-8 );
        assertEquals( 586.3154600610809, calc_std( data ), 1e-8 );

    }

    public static double[] normalize(double arr[], double p5, double p95) {
        double[] ret = new double[arr.length];
        for (int i = 0; i != arr.length; ++i) {
            ret[i] = (arr[i]-p5)/(p95-p5);
        }
        return ret;
    }


    public static double calc_mean(double[] arr) {
        double sum = 0;
        int count = 0;
        for (double d : arr) {
            sum += d;
            count += 1;
        }
        return sum/count;
    }

    public static double calc_std(double[] arr) {
        double sum = 0;
        double square_sum = 0;
        int count = arr.length;
        for (double d : arr) {
            sum += d;
            square_sum += d*d;
        }
        return Math.sqrt( square_sum/count - (sum/count)*(sum/count) );
    }
}