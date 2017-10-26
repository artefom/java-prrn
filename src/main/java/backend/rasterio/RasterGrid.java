package backend.rasterio;

import backend.utils.CoordUtils;
import backend.utils.HashUtils;
import backend.utils.Vec2d;
import backend.utils.Vec2i;
import org.gdal.gdal.*;
import org.gdal.osr.osr;
import org.gdal.osr.SpatialReference;

import java.awt.image.Raster;

/**
 *     Definition of a pixel grid, including
 the size and extent, and by implication the
 resolution and alignment.

 Methods are defined for relationships with
 other instances, including:

 * intersection()
 * union()
 * reproject()
 * alignedWith()
 * isComparable()

 Attributes defined on the object:

 * xMin
 * xMax
 * yMin
 * yMax
 * xRes
 * yRes
 * projection

 NOTE: The bounds defined the external corners of the image, i.e. the
 top-left corner of the top-left pixel, through to the bottom-right
 corner of the bottom-right pixel. This is in accordance with GDAL conventions.
 */
public class RasterGrid {

//    def __init__(self, geotransform=None, nrows=None, ncols=None, projection=None,
//                 xMin=None, xMax=None, yMin=None, yMax=None, xRes=None, yRes=None):

    // extent of current raster grid;
    private double xRes,yRes,xMin,yMin,xMax,yMax;
    private String projection;

    /**
     * Constructor with explicit internal values
     * @param xMin coordinate of reference image
     * @param xMax coordinate of reference image
     * @param yMin coordinate of reference image
     * @param yMax coordinate of reference image
     * @param xRes resolution of reference image
     * @param yRes resolution of reference image
     * @param projection projection WKT String
     */
    public RasterGrid(double xMin, double xMax, double yMin, double yMax,
                      double xRes, double yRes, String projection) {
        this.projection = projection;
        this.xRes = xRes;
        this.yRes = yRes;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    /**
     * Creates RasterGrid from GDAL Geotransform array (double[4]) and number of rows and columns
     * internal values are calculated as follows:
     * xRes = geotransform[1];
     * yRes = Math.abs(geotransform[5]);
     * xMin = geotransform[0];
     * yMax = geotransform[3];
     * xMax = xMin + ncols * xRes;
     * yMin = yMax - nrows * yRes;
     * @param geotransform GDAL geotransform
     * @param nrows number of rows
     * @param ncols number of columns
     * @param projection projection WKT String
     */

    public RasterGrid(double[] geotransform, int nrows, int ncols, String projection) {
        this.projection = projection;
        xRes = geotransform[1];
        yRes = Math.abs(geotransform[5]);
        xMin = geotransform[0];
        yMax = geotransform[3];
        xMax = xMin + ncols * xRes;
        yMin = yMax - nrows * yRes;
    }

    public static RasterGrid fromDataset(Dataset ds) {

        double[] geotransform = ds.GetGeoTransform();
        int ncols = ds.GetRasterXSize();
        int nrows = ds.GetRasterYSize();
        String projection = ds.GetProjection();

        return new RasterGrid(geotransform,ncols,nrows,projection);
    }

    /**
     * Returns True if pixel size of self is equal to that of other.
     * Currently only checks absolute equality, probably should
     * work out a tolerance.
     */
    public boolean equalPixSize( RasterGrid other ) {
        return (xRes == other.xRes && yRes == other.yRes);
    }

    /**
     * Returns True if the projection of self is the same as the
     * projection of other
     * @return true if projections are equal
     */
    public boolean equalProjection(RasterGrid other) {
        String selfProj = projection != null ? projection : "";
        String otherProj = other.projection != null ? other.projection : "";
        SpatialReference srSelf = new SpatialReference(selfProj);
        SpatialReference srOther = new SpatialReference(otherProj);

        return srSelf.IsSame(srOther) == 1;
    }

    /**
     * Checks whether self is comparable with other. Returns
     * True or False. Grids are comparable if they have equal pixel
     * size, and the same projection.
     * @param other RasterGrid to check of comparable
     * @return true if Raster Grids are comparable
     */
    public boolean isComparable(RasterGrid other) {
        boolean comparable = true;

        // Check resolution
        if (!equalPixSize(other)) comparable = false;
        // Check projection
        if (!equalProjection(other)) comparable = false;
        return comparable;
    }


    /**
     *
     * Returns True if self is aligned with other. This means that
     * they represent the same grid, with different extents.
     * Alignment is checked within a small tolerance, so that exact
     * floating point matches are not required. However, notionally it
     * is possible to get a match which shouldn't be. The tolerance is
     * calculated as::
     * tolerance = 0.01 * pixsize / npix
     * and if a mis-alignment is <= tolerance, it is assumed to be zero.
     * For further details, read the source code.
     */
    public boolean aligned_with( RasterGrid other) {

        if (!isComparable(other)) return false;

        //Calculate a tolerance, based on the pixel size and the number of pixels,
        //so that when the tolerance is accumulated across the whole grid, the
        //total still comes out to be well under a pixel.
            // First get the largest dimension of either grid

        int npix = getNumPix(xMax, xMin, xRes);
        npix = Math.max(npix, getNumPix(other.xMax, other.xMin, other.xRes));
        npix = Math.max(npix, getNumPix(yMax, yMin, yRes));
        npix = Math.max(npix, getNumPix(other.yMax, other.yMin, other.yRes));
        double res = Math.min(xRes, yRes);
        double tolerance = 0.001 * res / npix;

        double xMinSnapped = snapToGrid(xMin, other.xMin, xRes);
        if (Math.abs(xMinSnapped - xMin) > tolerance) return false;
        double yMaxSnapped = snapToGrid(yMax, other.yMax, yRes);
        if (Math.abs(yMaxSnapped - yMax) > tolerance) return false;

        return true;
    }


    /**
     * Returns a new instance which is the intersection
     * of self and other.
     * @param other RasterGrid to find intersection with
     * @return RasterGrid representing intersecting area
     */
    public RasterGrid intersection( RasterGrid other ) {

        if (!isComparable(other)) return null;

        double new_xMin = Math.max(xMin, other.xMin);
        double new_xMax = Math.min(xMax, other.xMax);
        double new_yMin = Math.max(yMin, other.yMin);
        double new_yMax = Math.min(yMax, other.yMax);

        if (new_xMin >= new_xMax || new_yMin >= new_yMax) return null;

        return new RasterGrid(new_xMin, new_xMax, new_yMin,
                new_yMax, xRes, yRes, projection);
    }

    /**
     *     Returns a GDAL geotransform tuple from bounds and resolution
     * @return GDAL geotransform: double[4]
     */
    public double[] makeGeoTransform() {
        return new double[]{xMin, xRes, 0.0, yMax, 0.0, -yRes};
    }

    /**
     * Utility method, return number of rows, or y size, or height of image
     * @return raster height
     */
    public int get_height() {
        return getNumPix(yMax, yMin, yRes);
    }

    /**
     * Utility method, returns number of columns or x size or width of image
     * @return raster width
     */
    public int get_width() {
        return getNumPix(xMax,xMin,xRes);
    }

    public Vec2d wld2pix(double x, double y) {
        return CoordUtils.wld2pix(makeGeoTransform(),x,y);

    }

    public Vec2d wld2pix(Vec2d coord) {
        return wld2pix(coord.x,coord.y);
    }

    public Vec2d wld2pix(Vec2i coord) {
        return wld2pix(coord.x,coord.y);
    }


    public Vec2d pix2wld(double x, double y) {
        return CoordUtils.wld2pix(makeGeoTransform(),x,y);
    }

    public Vec2d pix2wld(Vec2d coord) {
        return pix2wld(coord.x,coord.y);
    }

    public Vec2d pix2wld(Vec2i coord) {
        return pix2wld(coord.x,coord.y);
    }



    /**
     * Works out how many pixels lie between the given min and max,
     * at the given resolution. This is for internal use only.
     * @param gridMax Maximum coordinate
     * @param gridMin minimum coordinate
     * @param gridRes resolutrion
     * @return number of pixels
     */
    private static int getNumPix( double gridMax, double gridMin, double gridRes) {
        return (int)round_away((gridMax - gridMin) / gridRes);
    }

    /**
     * Rounds number AWAY from zero
     * -0.51 -> -1
     * -0.5 -> -1
     * -0.49 -> 0
     * 0.49 -> 0
     * 0.5 -> 1
     * 0.51 -> 1
     * @param val value to round
     * @return value rounded away from zero
     */
    private static long round_away(double val) {
        if (val < 0){
            return -Math.round(-val);
        } else {
            return Math.round(val);
        }
    }

    /**
     * Returns the nearest value to val which is a whole multiple of
     * res away from valOnGrid, so that val is effectively on the same
     * grid as valOnGrid. This is for internal use only.
     * @return
     */
    private static double snapToGrid(double val, double valOnGrid, double res) {
        double diff = val - valOnGrid;
        double numPix = diff / res;
        int numWholePix = (int)round_away(numPix);
        return valOnGrid + numWholePix * res;
    }

    @Override
    public int hashCode() {
        int[] hashcodes = new int[]{
                this.projection.hashCode(),
                HashUtils.hash(this.xRes),
                HashUtils.hash(this.yRes),
                HashUtils.hash(this.xMin),
        };
        return HashUtils.CombineHashCodes(hashcodes);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RasterDataset))return false;
        RasterGrid o = (RasterGrid)other;

        return
                this.projection == o.projection &&
        this.xRes == o.xRes &&
        this.yRes == o.yRes &&
        this.xMin == o.xMin &&
        this.yMin == o.yMin &&
        this.xMax == o.xMax &&
        this.yMax == o.yMax;
    }

    @Override
    protected RasterGrid clone() {
        return new RasterGrid(xMin, xMax, yMin, yMax, xRes, yRes, projection);
    }

}
