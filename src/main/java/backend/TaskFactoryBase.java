package backend;

import backend.rasterio.RasterDataset;

import java.io.IOException;

public interface TaskFactoryBase {
    TaskBase create_task(String f1, String f2) throws IOException;
    TaskBase create_task(RasterDataset ds1, RasterDataset d2);
}
