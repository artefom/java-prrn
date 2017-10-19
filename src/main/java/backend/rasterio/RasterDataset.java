package backend.rasterio;

import java.util.Random;

public class RasterDataset {

    private String filename;
    private static Random rand = new Random();

    private RasterDataset() {
        filename = "";
    }

    private void open(String fname) {
        filename = fname;
    }

    public String get_filename() {
        return filename;
    }

    public static RasterDataset from_file(String filename) {
        RasterDataset ret = new RasterDataset();
        ret.open(filename);
        return ret;
    }

    public double adjacent_weight(RasterDataset other) {
        double ret = rand.nextDouble();
        if (ret < 0.5) ret = 0;
        return ret;
    }


    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RasterDataset))return false;

        return filename.equals(((RasterDataset) other).filename);
    }

    @Override
    public String toString() {
        return String.format("%s",filename.substring(filename.length()-15,filename.length()-4) );
    }
}
