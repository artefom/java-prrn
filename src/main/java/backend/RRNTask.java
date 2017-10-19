package backend;

import backend.rasterio.RasterDataset;

import java.util.logging.Logger;

public class RRNTask implements TaskBase {

    // Logger
    private static Logger log = Logger.getLogger(RRNTask.class.getName());

    @Override
    public void assign_datasets(RasterDataset ds1, RasterDataset ds2) {

    }

    @Override
    public double[] read_batch(int batch_size) {
        return new double[0];
    }

    @Override
    public void reset() {

    }
}
