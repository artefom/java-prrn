package backend.tasks;

import backend.rasterio.RasterDataset;

class RRNTask implements IRRNTask {

    private RasterDataset source;
    private RasterDataset target;

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
}
