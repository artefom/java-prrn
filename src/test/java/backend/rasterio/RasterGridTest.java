package backend.rasterio;

import backend.utils.CoordUtils;
import backend.utils.Vec2d;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RasterGridTest {

    private Dataset ds1;
    private Dataset ds2;
    private RasterGrid g1;
    private RasterGrid g2;
    private static final double DELTA = 1e-15;

    /**
     * Construct test class.
     * Initialize test datasets only once!
     */
    public RasterGridTest() {
        gdal.AllRegister();

        String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
        String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

        ds1 = gdal.Open(path1, gdalconstConstants.GA_ReadOnly);
        ds2 = gdal.Open(path2, gdalconstConstants.GA_ReadOnly);
    }

    @Before
    public void create_raster_girds() {
        g1 = RasterGrid.fromDataset(ds1);
        g2 = RasterGrid.fromDataset(ds2);
    }

    @Test
    public void intersection() throws Exception {
        RasterGrid gi = g1.intersection(g2);

        // Make sure width and height matches
        assertEquals(700,gi.get_width());
        assertEquals(930,gi.get_height());

        // Make sure geotransform matches!
        double[] transofrm_expected = new double[]{376485.0, 30.0, 0.0, 6156555.0, 0.0, -30.0};
        double[] transofrm_got = gi.makeGeoTransform();
        for (int i = 0; i != transofrm_got.length; ++i) {
            assertEquals(transofrm_expected[i],transofrm_got[i],DELTA);
        }
    }


    @Test
    public void fromDataset() throws Exception{
        //Make sure file dimensions match
        assertEquals(700,ds1.GetRasterXSize());
        assertEquals(1000,ds1.GetRasterYSize());
        assertEquals(1000,ds2.GetRasterXSize());
        assertEquals(1000,ds2.GetRasterYSize());
    }

    @Test
    public void equalPixSize() throws Exception {
        assertTrue(g1.equalPixSize(g2));

        // Raster grid with different size
        RasterGrid g3 = new RasterGrid(g1.makeGeoTransform(),10,10,g1.get_projection());
        assertTrue(g1.equalPixSize(g3));
        assertTrue(g2.equalPixSize(g3));

        // Read gotransform to array so we can change it later
        double[] geotransform = g2.makeGeoTransform();
        // Change resolution of geotransform by 0.001
        geotransform[1] += 0.001;
        RasterGrid g4 = new RasterGrid(geotransform,10,10,g1.get_projection());
        assertFalse(g1.equalPixSize(g4));
        assertFalse(g2.equalPixSize(g4));
        assertFalse(g3.equalPixSize(g4));
    }

    @Test
    public void equalProjection() throws Exception {
        assertTrue(g1.equalProjection(g2));
    }

    @Test
    public void isComparable() throws Exception {
        assertTrue(g1.isComparable(g2));
    }

    @Test
    public void aligned_with() throws Exception {
        assertTrue(g1.aligned_with(g2));
    }

    @Test
    public void makeGeoTransform() throws Exception {
        double[] transofrm_original = ds1.GetGeoTransform();
        double[] transofrm_got = g1.makeGeoTransform();
        for (int i = 0; i != transofrm_got.length; ++i) {
            assertEquals(transofrm_original[i],transofrm_got[i],DELTA);
        }

        transofrm_original = ds2.GetGeoTransform();
        transofrm_got = g2.makeGeoTransform();
        for (int i = 0; i != transofrm_got.length; ++i) {
            assertEquals(transofrm_original[i],transofrm_got[i],DELTA);
        }
    }

    @Test
    public void wld2pix_test1() throws Exception {
        Vec2d ret = g1.wld2pix(0,0);
        assertEquals(-12549.5,ret.x, DELTA);
        assertEquals(205288.5,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test1() throws Exception {
        Vec2d ret = g1.pix2wld(0,0);
        assertEquals(376485.0,ret.x, DELTA);
        assertEquals(6158655.0,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test2() throws Exception {
        Vec2d ret = g1.wld2pix(0.5,0.5);
        assertEquals(-12549.483333333334,ret.x, DELTA);
        assertEquals(205288.48333333334,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test2() throws Exception {
        Vec2d ret = g1.pix2wld(0.5,0.5);
        assertEquals(376500.0,ret.x, DELTA);
        assertEquals(6158640.0,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test3() throws Exception {
        Vec2d ret = g1.wld2pix(1.49,1.49);
        assertEquals(-12549.450333333334,ret.x, DELTA);
        assertEquals(205288.45033333334,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test3() throws Exception {
        Vec2d ret = g1.pix2wld(1.49,1.49);
        assertEquals(376529.7,ret.x, DELTA);
        assertEquals(6158610.3,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test4() throws Exception {
        Vec2d ret = g1.wld2pix(1.5,1.5);
        assertEquals(-12549.45,ret.x, DELTA);
        assertEquals(205288.45,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test4() throws Exception {
        Vec2d ret = g1.pix2wld(1.5,1.5);
        assertEquals(376530.0,ret.x, DELTA);
        assertEquals(6158610.0,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test5() throws Exception {
        Vec2d ret = g1.wld2pix(1.51,1.51);
        assertEquals(-12549.449666666666,ret.x, DELTA);
        assertEquals(205288.44966666665,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test5() throws Exception {
        Vec2d ret = g1.pix2wld(1.51,1.51);
        assertEquals(376530.3,ret.x, DELTA);
        assertEquals(6158609.7,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test6() throws Exception {
        Vec2d ret = g1.wld2pix(-0.5,-0.5);
        assertEquals(-12549.516666666666,ret.x, DELTA);
        assertEquals(205288.51666666666,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test6() throws Exception {
        Vec2d ret = g1.pix2wld(-0.5,-0.5);
        assertEquals(376470.0,ret.x, DELTA);
        assertEquals(6158670.0,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test7() throws Exception {
        Vec2d ret = g1.wld2pix(-1.49,-1.49);
        assertEquals(-12549.549666666666,ret.x, DELTA);
        assertEquals(205288.54966666666,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test7() throws Exception {
        Vec2d ret = g1.pix2wld(-1.49,-1.49);
        assertEquals(376440.3,ret.x, DELTA);
        assertEquals(6158699.7,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test8() throws Exception {
        Vec2d ret = g1.wld2pix(-1.5,-1.5);
        assertEquals(-12549.55,ret.x, DELTA);
        assertEquals(205288.55,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test8() throws Exception {
        Vec2d ret = g1.pix2wld(-1.5,-1.5);
        assertEquals(376440.0,ret.x, DELTA);
        assertEquals(6158700.0,ret.y, DELTA);
    }

    @Test
    public void wld2pix_test9() throws Exception {
        Vec2d ret = g1.wld2pix(-1.51,-1.51);
        assertEquals(-12549.550333333334,ret.x, DELTA);
        assertEquals(205288.55033333335,ret.y, DELTA);
    }

    @Test
    public void pix2wld_test9() throws Exception {
        Vec2d ret = g1.pix2wld(-1.51,-1.51);
        assertEquals(376439.7,ret.x, DELTA);
        assertEquals(6158700.3,ret.y, DELTA);
    }


}