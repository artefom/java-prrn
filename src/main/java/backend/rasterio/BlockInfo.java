package backend.rasterio;

import backend.utils.Vec2d;

/**
 * Class contains information about block,
 * including it's data and geographic coordinates and projection
 */
public class BlockInfo {

    double[][] layers;
    int width;
    int height;
    int totalxblocks;
    int totalyblocks;

    Vec2d tl;
    Vec2d br;

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

    public Vec2d get_br_coord() {
        return this.br;
    }

    public Vec2d get_tl_coord() {
        return this.tl;
    }

    /**
     * According to gdal, layer enumeration begins from 1.
     * Preserve it here.
     * @param layer_id Index of layer beginning from 1
     * @return array of dataset values
     */
    public double[] get_layer(int layer_id) {
        return layers[layer_id-1];
    }

    public double[][] get_data() {
        return layers;
    }


}
