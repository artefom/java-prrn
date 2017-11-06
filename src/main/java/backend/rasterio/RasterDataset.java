package backend.rasterio;

import java.io.IOException;
import java.util.logging.Logger;

import org.gdal.gdal.*;
import org.gdal.gdalconst.gdalconstConstants;

/**
 * Class encapsulates 2 entities
 * First one is GDAL dataset.
 * it contains all info about image itself, allows to read pixels, etc..
 * The second one is RasterGrid
 * which can yield intersections of images and answer following questions:
 * Do images have same projection?
 * Are images aligned?
 * Are images comparable (pixel-wise)?
 * What is their intersection?
 */
public class RasterDataset {

    // Logger
    private static Logger log = Logger.getLogger(RasterDataset.class.getName());

    private Dataset ds;
    private RasterGrid rgrid;

    private RasterDataset() {
    }


    private void open(String fname) throws IOException {
        ds = gdal.Open(fname,gdalconstConstants.GA_ReadOnly);
        if (ds == null) {
            throw new IOException("Could not open " + fname);
        }
        rgrid = RasterGrid.fromDataset(ds);
    }

    /**
     * Get filename of currently opened dataset
     * @return
     */
    public String get_filename() {
        if (ds == null) return "";
        return ds.GetDescription();
    }

    public int get_type() {
        return ds.GetRasterBand(1).getDataType();
    }
    public int block_width() {return ds.GetRasterBand(1).GetBlockXSize();}
    public int block_height() {return ds.GetRasterBand(1).GetBlockYSize();}

    public void delete() {
        ds.delete();
    }

    /**
     * Create dataset from file
     * @param filename
     * @return
     * @throws IOException
     */
    public static RasterDataset from_file(String filename) throws IOException {
        RasterDataset ret = new RasterDataset();
        ret.open(filename);
        return ret;
    }

    public RasterGrid grid() {
        return rgrid;
    }
    public Dataset dataset(){return ds;};

    @Override
    public int hashCode() {
        return get_filename().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RasterDataset))return false;

        return get_filename().equals(((RasterDataset) other).get_filename());
    }

    @Override
    public String toString() {
        String filename = get_filename();
        if (filename.length() < 16) {
            return filename;
        }
        return String.format("%s",filename.substring(filename.length()-15,filename.length()-4) );
    }
}
