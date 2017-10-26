package backend.rasterio;

import com.sun.xml.internal.ws.util.ReadAllStream;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Before;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class RasterGridTest {

    private Dataset ds1;
    private Dataset ds2;

    @Before
    public void init() {
        if (ds1 == null || ds2 == null) {

            String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
            String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

            ds1 = gdal.Open(path1, gdalconstConstants.GA_ReadOnly);
            ds2 = gdal.Open(path2, gdalconstConstants.GA_ReadOnly);

        }
    }

    @Test
    public void fromDataset() throws Exception{
        System.out.println(ds1.GetRasterXSize());
    }

    @Test
    public void equalPixSize() throws Exception {
    }

    @Test
    public void equalProjection() throws Exception {
    }

    @Test
    public void isComparable() throws Exception {
    }

    @Test
    public void aligned_with() throws Exception {
    }

    @Test
    public void intersection() throws Exception {
    }

    @Test
    public void makeGeoTransform() throws Exception {
    }

    @Test
    public void get_height() throws Exception {
    }

    @Test
    public void get_width() throws Exception {
    }

    @Test
    public void wld2pix() throws Exception {
    }

    @Test
    public void wld2pix1() throws Exception {
    }

    @Test
    public void wld2pix2() throws Exception {
    }

    @Test
    public void pix2wld() throws Exception {
    }

    @Test
    public void pix2wld1() throws Exception {
    }

    @Test
    public void pix2wld2() throws Exception {
    }

}