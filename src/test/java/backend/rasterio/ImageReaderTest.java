package backend.rasterio;

import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Test;

import java.awt.image.Raster;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test class for ImageReader
 */
public class ImageReaderTest {

    RasterDataset ds1;
    RasterDataset ds2;

    public ImageReaderTest() throws IOException {
        //Initialize gdal!
        gdal.AllRegister();

        String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
        String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

        ds1 = RasterDataset.from_file(path1);
        ds2 = RasterDataset.from_file(path2);
    }

    @Test
    public void test_read() {
        ImageReader reader = ImageReader.from_datasets(ds1,ds2);
        reader.set_window_size(1000,1000);
        reader.init();

        BlockInfo info = reader.read_block(0,0);

        assertEquals(700,info.get_block_width());
        assertEquals(930,info.get_block_height());

        // Make sure we have read 2 files
        assertEquals(info.data.size(),2);
        assertEquals(info.datasets.size(), 2);

        // Make sure we have read all 12 bands
        assertEquals(info.data.get(0).length,12);
        assertEquals(info.data.get(1).length,12);

        // Make sure number of pixels is correct
        for (int j = 0; j != 12; ++j) {
            assertEquals(930 * 700, info.data.get(0)[j].length);
        }
        // Now, make sure that pixel values are valid by calculating statistics

        // Calculate statistics on first layer of first dataset
        System.out.println("STATS: "+calc_mean( info.data.get(0)[0] ));
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
}