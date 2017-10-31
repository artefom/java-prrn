package backend.rasterio;

import backend.utils.Vec2d;
import backend.utils.Vec2i;
import com.sun.xml.internal.ws.util.ReadAllStream;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Vector;

/**
 * Generates a set of blocks to be read from multiple files
 * Blocks are aligned to match dataset's blocks
 */
public class BlockGenerator {

    enum READ_AREA { INTERSECTION };

    RasterDataset[] datasets;
    RasterGrid read_grid;
    int bw;
    int bh;

    // Dataset to use as reference for block calculations
    RasterDataset block_ref_ds;

    public BlockGenerator(RasterDataset[] datasets, RasterGrid read_grid) {
        this.datasets = datasets;
        this.read_grid = read_grid;
        block_ref_ds = datasets[0];
        bw = block_ref_ds.block_width();
        bh = block_ref_ds.block_height();
    }

    public int get_n_datasets() {
        return datasets.length;
    }

    public int get_n_blocks_width() {
        Vec2i px_min = block_ref_ds.grid().wld2pix(read_grid.xMin,read_grid.yMin).round();
        Vec2i px_max = block_ref_ds.grid().wld2pix(read_grid.xMax,read_grid.yMax).round();
        int swap_buf;
        if (px_min.x > px_max.x) {swap_buf = px_min.x; px_min.x = px_max.x; px_max.x = swap_buf;}

        // Always prefer integer arithmetic over double!
        int block_xbeg = (px_min.x/bw);
        int block_xend = (px_max.x/bw+(px_max.x%bw == 0 ? 0 : 1));

        return (block_xend-block_xbeg);
    }

    public int get_n_blocks_height() {
        Vec2i px_min = block_ref_ds.grid().wld2pix(read_grid.xMin,read_grid.yMin).round();
        Vec2i px_max = block_ref_ds.grid().wld2pix(read_grid.xMax,read_grid.yMax).round();
        int swap_buf;
        if (px_min.y > px_max.y) {swap_buf = px_min.y; px_min.y = px_max.y; px_max.y = swap_buf;}

        // Always prefer integer arithmetic over double!
        int block_ybeg = (px_min.y/bh);
        int block_yend = (px_max.y/bh+(px_max.y%bh == 0 ? 0 : 1));

        return (block_yend-block_ybeg);
    }

    public int get_n_blocks() {
        return get_n_blocks_width()*get_n_blocks_height();
    }

    public void get_block_world_extents(int dataset_id, int block_x, int block_y, Vec2i out_min, Vec2i out_max) {
        // Having world coordinates of area to read
        // Calculate pixel boundaries of read area for current dataset
        Vec2i px_min = block_ref_ds.grid().wld2pix(read_grid.xMin,read_grid.yMin).round();
        Vec2i px_max = block_ref_ds.grid().wld2pix(read_grid.xMax,read_grid.yMax).round();
        int swap_buf;
        if (px_min.x > px_max.x) {swap_buf = px_min.x; px_min.x = px_max.x; px_max.x = swap_buf;}
        if (px_min.y > px_max.y) {swap_buf = px_min.y; px_min.y = px_max.y; px_max.y = swap_buf;}

        // Calculate ids of start block
        int block_xbeg = (px_min.x/bw);
        int block_ybeg = (px_min.y/bh);

        // Calculate area to read in world coordinates
        Vec2d wld_block_corner1 = block_ref_ds.grid().pix2wld( (block_xbeg+block_x)*bw, (block_ybeg+block_y)*bh);
        Vec2d wld_block_corner2 = block_ref_ds.grid().pix2wld( (block_xbeg+block_x+1)*bw, (block_ybeg+block_y+1)*bh);

        RasterDataset ret_ref_ds = datasets[dataset_id];

        out_min.set( ret_ref_ds.grid().wld2pix(wld_block_corner1).round() );
        out_max.set( ret_ref_ds.grid().wld2pix(wld_block_corner2).round() );

        // Make sure min/max condition is satisfied
        if (out_min.x > out_max.x) {swap_buf = out_min.x; out_min.x = out_max.x; out_max.x = swap_buf;}
        if (out_min.y > out_max.y) {swap_buf = out_min.y; out_min.y = out_max.y; out_max.y = swap_buf;}
    }

    /**
     * Find intersection grid of multiple datasets
     * @param datasets datasets to find intersection of
     * @return raster grid representing area included in each dataset
     */
    public static RasterGrid intersection(RasterDataset[] datasets) {
        RasterGrid intersection_grid = datasets[0].grid();

        for (int i = 1; i < datasets.length; ++i) {
            if (!intersection_grid.isComparable(datasets[i].grid()))
                throw new IllegalArgumentException("Dataset grids must have equal projection and pixel coordinates");
            intersection_grid = intersection_grid.intersection(datasets[i].grid());
        }
        return intersection_grid;
    };

    public static BlockGenerator from_datasets(RasterDataset[] datasets, READ_AREA read_area) {
        RasterGrid read_grid = null;
        if (read_area == READ_AREA.INTERSECTION) {
            read_grid = intersection(datasets);

            return new BlockGenerator(datasets,read_grid);
        } else {
            throw new IllegalArgumentException("Not implemented for read_area = "+read_area.name());
        }
    }

    /**
     * Calculate number of blocks contained int specific area
     * @param bw width of block
     * @param bh height of block
     * @param xoff x offset of area
     * @param yoff y offset of area
     * @param xsize x size of area
     * @param ysize y size of area
     * @return number of blocks intersecting with the area
     */
    public static int calc_block_num(int bw, int bh, int xoff, int yoff, int xsize, int ysize) {

        int xend = xoff + xsize;
        int yend = yoff + ysize;

        // Always prefer integer arithmetic over double!
        int block_xbeg = (xoff/bw);
        int block_xend = (xend/bw+(xend%bw == 0 ? 0 : 1)); // Make sure we add 1 if there is an excess
        int block_ybeg = (yoff/bh);
        int block_yend = (yend/bh+(yend%bh == 0 ? 0 : 1));

        // Keep code nice and tidy, don't inline!
        return (block_xend-block_xbeg)*(block_yend-block_ybeg);
    }


}
