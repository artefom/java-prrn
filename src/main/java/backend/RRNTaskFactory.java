package backend;

import backend.rasterio.RasterDataset;


public class RRNTaskFactory implements TaskFactoryBase {

    public TaskBase create_task(RasterDataset ds1, RasterDataset ds2) {
        RRNTask ret = new RRNTask();
        ret.assign_datasets(ds1,ds2);
        return ret;
    }

    public TaskBase create_task(String f1, String f2) {
        RRNTask ret = new RRNTask();
        ret.assign_datasets(RasterDataset.from_file(f1),RasterDataset.from_file(f2));
        return ret;
    }
}
