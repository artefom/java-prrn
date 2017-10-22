package backend.tasks;

import backend.rasterio.*;

public interface IRRNTask {

    void set_target(RasterDataset ds);
    RasterDataset get_target();

    void set_source(RasterDataset ds);
    RasterDataset get_source();

}
