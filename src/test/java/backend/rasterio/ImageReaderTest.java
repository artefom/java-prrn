package backend.rasterio;

import backend.utils.BufferUtils;
import backend.utils.TypeUtils;
import org.gdal.gdal.gdal;
import org.junit.Test;

import java.io.IOException;

import static backend.utils.MathUtils.mean;
import static backend.utils.MathUtils.std;
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

        assertEquals(expected_width,info.width);
        assertEquals(expected_height,info.height);

        // Make sure we have read 2 files
        assertEquals(info.n_datasets(),2);

        int[] x_block_size = new int[1];
        int[] y_block_size = new int[1];

        info.get_dataset(0).GetRasterBand(1).GetBlockSize(x_block_size,y_block_size);

//        System.out.println("Block x size: "+x_block_size[0]);
//        System.out.println("Block y size: "+y_block_size[0]);

        // Make sure we have read all 12 bands
        assertEquals(info.n_bands(),12);

        // Make sure number of pixels is correct

        for (int band_id = 0; band_id != 12; ++band_id) {
            int data_type = info.get_dataset(0).GetRasterBand(band_id+1).getDataType();
            int pixel_num = info.get_data(0,band_id).remaining()/ TypeUtils.get_size(data_type);
            assertEquals(expected_width * expected_height, pixel_num);
        }
        // Now, make sure that pixel values are valid by calculating statistics

        // Make sure statistics match for area read

        //Dataset 1,  Band 1
        int data_type = info.get_dataset(0).GetRasterBand(1).getDataType();
        double[] data = BufferUtils.toDoubleArr( info.get_data(0,0), data_type );
        assertEquals( 8513.5624178187409, mean( data ), 1e-8 );
        assertEquals( 576.95433514124056, std( data ), 1e-8 );

        //Dataset 2,  Band 1
        data_type = info.get_dataset(1).GetRasterBand(1).getDataType();
        data = BufferUtils.toDoubleArr( info.get_data(1,0), data_type );
        assertEquals( 9949.9581536098303, mean( data ), 1e-8 );
        assertEquals( 586.3154600610809, std( data ), 1e-8 );

    }

    public static double[] normalize(double arr[], double p5, double p95) {
        double[] ret = new double[arr.length];
        for (int i = 0; i != arr.length; ++i) {
            ret[i] = (arr[i]-p5)/(p95-p5);
        }
        return ret;
    }

}