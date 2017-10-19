package backend;

import backend.rasterio.RasterDataset;

public interface TaskFactoryBase {
    TaskBase create_task(String f1, String f2);
    TaskBase create_task(RasterDataset ds1, RasterDataset d2);
}
