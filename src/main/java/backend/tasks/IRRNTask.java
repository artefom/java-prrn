package backend.tasks;

import backend.rasterio.*;
import backend.rrn.RRNResult;

public interface IRRNTask {

    void set_target(RasterDataset ds);
    RasterDataset get_target();

    void set_source(RasterDataset ds);
    RasterDataset get_source();

    void set_result(RRNResult res);
    RRNResult get_result();

}
