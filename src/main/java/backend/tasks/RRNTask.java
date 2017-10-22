package backend.tasks;

import backend.rasterio.RasterDataset;
import backend.rrn.RRNResult;

class RRNTask implements IRRNTask {

    private RasterDataset source;
    private RasterDataset target;
    private RRNResult res;

    public RRNTask() {

    }

    @Override
    public void set_target(RasterDataset ds) {
        target = ds;
    }

    @Override
    public RasterDataset get_target() {
        return target;
    }

    @Override
    public void set_source(RasterDataset ds) {
        source = ds;
    }

    @Override
    public RasterDataset get_source() {
        return source;
    }

    @Override
    public void set_result(RRNResult res) {
        this.res = res;
    }

    @Override
    public RRNResult get_result() {
        return res;
    }
}
