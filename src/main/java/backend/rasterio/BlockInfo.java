package backend.rasterio;

import backend.utils.Vec2d;
import org.gdal.gdal.Dataset;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Class contains information about block,
 * including it's data and geographic coordinates and projection
 */
public class BlockInfo {

    /**
     * Data storage arranged by separate files
     */
    public ArrayList<ByteBuffer[]> data;
    public ArrayList<Dataset> datasets;

    public RasterGrid grid;

    public int width;
    public int height;
    public int totalxblocks;
    public int totalyblocks;

    public Vec2d tl;
    public Vec2d br;

    BlockInfo() {

    }

    public void set_totalblocks(int x, int y) {
        this.totalxblocks = x;
        this.totalyblocks = y;
    }

    public void set_block_size(int width,int height) {
        this.width = width;
        this.height = height;
    }

    public int get_block_width() {
        return width;
    }

    public int get_block_height() {
        return height;
    }

    public void set_block_bounds(Vec2d block_tl, Vec2d block_br) {
        this.tl = block_tl;
        this.br = block_br;
    }


}
