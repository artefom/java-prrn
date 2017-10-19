package backend;

import backend.rasterio.RasterDataset;

public interface TaskBase {

    // Assigns pair of files to task
    void assign_datasets(RasterDataset ds1, RasterDataset ds2);

    // Read batch of pixels from images
    double[] read_batch(int batch_size);

    void reset();
}
