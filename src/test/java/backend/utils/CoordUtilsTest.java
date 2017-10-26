package backend.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoordUtilsTest {

    static String projection;
    static double[] transform;
    Vec2d in_vec;
    Vec2d out_vec;

    private static final double DELTA = 1e-15;

    @Before
    public void init() {
        //projection = "PROJCS[\"WGS 84 / UTM zone 37N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",39],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AUTHORITY[\"EPSG\",\"32637\"]]";
        transform = new double[]{346125.0, 30.0, 0.0, 6140925.0, 0.0, -30.0};
    }

    @Test
    public void wld2pix_test1() throws Exception {
        in_vec = new Vec2d(0,0);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.5,out_vec.x,DELTA);
        assertEquals(204697.5,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test1() throws Exception {
        in_vec = new Vec2d(0,0);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346125.0,out_vec.x,DELTA);
        assertEquals(6140925.0,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test2() throws Exception {
        in_vec = new Vec2d(0.5,0.5);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.483333333334,out_vec.x,DELTA);
        assertEquals(204697.48333333334,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test2() throws Exception {
        in_vec = new Vec2d(0.5,0.5);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346140.0,out_vec.x,DELTA);
        assertEquals(6140910.0,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test3() throws Exception {
        in_vec = new Vec2d(1.49,1.49);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.450333333334,out_vec.x,DELTA);
        assertEquals(204697.45033333334,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test3() throws Exception {
        in_vec = new Vec2d(1.49,1.49);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346169.7,out_vec.x,DELTA);
        assertEquals(6140880.3,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test4() throws Exception {
        in_vec = new Vec2d(1.5,1.5);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.45,out_vec.x,DELTA);
        assertEquals(204697.45,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test4() throws Exception {
        in_vec = new Vec2d(1.5,1.5);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346170.0,out_vec.x,DELTA);
        assertEquals(6140880.0,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test5() throws Exception {
        in_vec = new Vec2d(1.51,1.51);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.449666666666,out_vec.x,DELTA);
        assertEquals(204697.44966666665,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test5() throws Exception {
        in_vec = new Vec2d(1.51,1.51);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346170.3,out_vec.x,DELTA);
        assertEquals(6140879.7,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test6() throws Exception {
        in_vec = new Vec2d(-0.5,-0.5);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.516666666666,out_vec.x,DELTA);
        assertEquals(204697.51666666666,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test6() throws Exception {
        in_vec = new Vec2d(-0.5,-0.5);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346110.0,out_vec.x,DELTA);
        assertEquals(6140940.0,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test7() throws Exception {
        in_vec = new Vec2d(-1.49,-1.49);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.549666666666,out_vec.x,DELTA);
        assertEquals(204697.54966666666,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test7() throws Exception {
        in_vec = new Vec2d(-1.49,-1.49);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346080.3,out_vec.x,DELTA);
        assertEquals(6140969.7,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test8() throws Exception {
        in_vec = new Vec2d(-1.5,-1.5);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.55,out_vec.x,DELTA);
        assertEquals(204697.55,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test8() throws Exception {
        in_vec = new Vec2d(-1.5,-1.5);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346080.0,out_vec.x,DELTA);
        assertEquals(6140970.0,out_vec.y,DELTA);
    }

    @Test
    public void wld2pix_test9() throws Exception {
        in_vec = new Vec2d(-1.51,-1.51);
        out_vec = CoordUtils.wld2pix(transform,in_vec.x,in_vec.y);
        assertEquals(-11537.550333333334,out_vec.x,DELTA);
        assertEquals(204697.55033333335,out_vec.y,DELTA);
    }

    @Test
    public void pix2wld_test9() throws Exception {
        in_vec = new Vec2d(-1.51,-1.51);
        out_vec = CoordUtils.pix2wld(transform,in_vec.x,in_vec.y);
        assertEquals(346079.7,out_vec.x,DELTA);
        assertEquals(6140970.3,out_vec.y,DELTA);
    }

}