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

        BlockInfo i = reader.read_block(0,0);

        assertEquals(700,i.get_block_width());
        assertEquals(930,i.get_block_height());

        double[] layer_data = i.get_layer(0);
    }
}