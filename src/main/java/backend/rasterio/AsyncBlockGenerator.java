package backend.rasterio;

import backend.utils.Vec2d;
import backend.utils.Vec2i;

/**
 * Generates a sequence of blocks to be read from multiple files
 * Blocks are aligned to match dataset's blocks
 */
public class AsyncBlockGenerator {

    enum READ_AREA { INTERSECTION };

    // Make all variables final to make sure they are not changed in concurrent calls
    private final RasterDataset[] datasets;

    private final RasterGrid read_grid;

    private final int byoff;
    private final int bxoff;
    private final int bw;
    private final int bh;

    private final int n_horizontal_blocks;
    private final int n_vertical_blocks;
    private final int n_blocks;

    // x and y position of tl block and bottom right block
    private final int block_xbeg;
    private final int block_ybeg;
    private final int block_xend;
    private final int block_yend;

    private final Vec2i ref_px_min;
    private final Vec2i ref_px_max;

    // Dataset to use as reference for block calculations
    // private final RasterDataset block_ref_ds;

    // The only non-final variable. Make sure to access it in sync method
    private int cur_block = -1;

    public AsyncBlockGenerator(RasterDataset[] i_datasets, RasterGrid i_read_grid,
                               int i_bxoff, int i_byoff, int i_bsizex, int i_bsizey) {
        datasets = i_datasets;
        read_grid = i_read_grid;

        // Set reference dataset. This dataset is used to get block coordinates.
        // We just read areas, matching blocks of this dataset
        // and hope that they also match blocks of other datasets
        //block_ref_ds = datasets[0];

        // Cache block width, height, number of horizontal and vertical blocks
        bw = i_bsizex;
        bh = i_bsizey;

        // Calculate offsets so they are always negative and don't exceed block size
        bxoff = i_bxoff%bw - (i_bxoff%bw > 0 ? bw : 0);
        byoff = i_byoff%bh - (i_byoff%bh > 0 ? bh : 0);

        n_horizontal_blocks = get_n_blocks(read_grid.get_width() ,bxoff,bw);
        n_vertical_blocks   = get_n_blocks(read_grid.get_height(),byoff,bh);

        n_blocks = n_horizontal_blocks*n_vertical_blocks;

        // Calculate read area boundaries in terms of reference dataset
        ref_px_min = i_read_grid.wld2pix(read_grid.xMin,read_grid.yMin).round();
        ref_px_max = i_read_grid.wld2pix(read_grid.xMax,read_grid.yMax).round();
        int swap_buf;
        if (ref_px_min.x > ref_px_max.x) {swap_buf = ref_px_min.x; ref_px_min.x = ref_px_max.x; ref_px_max.x = swap_buf;}
        if (ref_px_min.y > ref_px_max.y) {swap_buf = ref_px_min.y; ref_px_min.y = ref_px_max.y; ref_px_max.y = swap_buf;}

        // Calculate ids of start block
        block_xbeg = (ref_px_min.x/bw);
        block_ybeg = (ref_px_min.y/bh);

        // Calculate ids of end block
        block_xend = (ref_px_max.x/bw+(ref_px_max.x%bw == 0 ? 0 : 1));
        block_yend = (ref_px_max.y/bh+(ref_px_max.y%bh == 0 ? 0 : 1));

    }

    public synchronized int get_next_block_id() {
        cur_block += 1;
        if (cur_block >= n_blocks) return -1;
        return cur_block;
    }

    public int get_n_datasets() {
        return datasets.length;
    }

    public int get_n_blocks() {
        return n_blocks;
    }

    public int get_n_horizontal_blocks() {
        return n_horizontal_blocks;
    }

    public int get_n_vertical_blocks() {
        return n_vertical_blocks;
    }

    /**
     * Convert x and y block position to it's id
     * @param x x index of block
     * @param y y index of block
     * @return index of block
     */
    public int get_block_id(int x, int y) {
        return (x+y*n_horizontal_blocks);
    }

    /**
     * Convert one dim index to 2 dim index of block
     * @param block_id index of block get x and y indexes for
     * @param out output for x and y indexes of block
     */
    public void get_block_xyindex(int block_id, Vec2i out) {
        out.y = (block_id/n_horizontal_blocks);
        out.x = (block_id%n_horizontal_blocks);
    }

    /**
     * Convert one dim index to 2 dim index of block
     * @param block_id index of block get x and y indexes for
     * @return x and y indexes of block
     */
    public Vec2i get_block_xyindex(int block_id) {
        return new Vec2i( block_id%n_horizontal_blocks, block_id/n_horizontal_blocks);
    }

    /**
     * Same as {@link #get_block_world_coordinates(int, int, Vec2d, Vec2d) } but for block_id
     * @param block_id
     * @param out_min
     * @param out_max
     */
    public void get_block_world_coordinates(int block_id, Vec2d out_min, Vec2d out_max) {
        Vec2i block_coords = get_block_xyindex(block_id);
        get_block_world_coordinates(block_coords.x,block_coords.y,out_min,out_max);
    }


    /**
     * Calculate world coordinates of specific block
     * @param block_x x index of block
     * @param block_y y index of block
     * @param out_min output min boundary
     * @param out_max output max boundary
     */
    public void get_block_world_coordinates(int block_x, int block_y, Vec2d out_min, Vec2d out_max) {
        // Calculate area to read in world coordinates

        int block_px_x_beg = block_x*bw+bxoff;
        int block_px_y_beg = block_y*bh+byoff;
        int block_px_x_end = (block_x+1)*bw+bxoff;
        int block_px_y_end = (block_y+1)*bh+byoff;

        // Clamp values on borders
        if (block_px_x_beg < 0) block_px_x_beg = 0;
        if (block_px_y_beg < 0) block_px_y_beg = 0;

        if (block_px_x_end > read_grid.get_width()) block_px_x_end = read_grid.get_width();
        if (block_px_y_end > read_grid.get_height()) block_px_y_end = read_grid.get_height();

        out_min.set( read_grid.pix2wld( block_px_x_beg, block_px_y_beg) );
        out_max.set( read_grid.pix2wld( block_px_x_end, block_px_y_end) );

        // Make sure min/max relation is satisfied
        double swap_buf;
        if (out_min.x > out_max.x) {swap_buf = out_min.x; out_min.x = out_max.x; out_max.x = swap_buf;}
        if (out_min.y > out_max.y) {swap_buf = out_min.y; out_min.y = out_max.y; out_max.y = swap_buf;}
    }

    /**
     * Same as {@link #get_block_pixel_coordinates(RasterGrid, int, int, Vec2i, Vec2i)} buf for block index
     */
    public void get_block_pixel_coordinates(RasterGrid refgrid, int block_id, Vec2i out_min, Vec2i out_max) {
        Vec2i block_coords = get_block_xyindex(block_id);
        get_block_pixel_coordinates(refgrid, block_coords.x, block_coords.y, out_min, out_max);
    }

    /**
     * Get block coordinates in pixel terms of specific raster grid
     * @param refgrid returns pixel coordinates of blocks for this reference grid
     * @param block_x x index of block
     * @param block_y y index of block
     * @param out_min output min boundaries
     * @param out_max output max boundaries
     */
    public void get_block_pixel_coordinates(RasterGrid refgrid, int block_x, int block_y, Vec2i out_min, Vec2i out_max) {
        // Get coordinates of block in world terms
        Vec2d block_world_tl = new Vec2d();
        Vec2d block_world_br = new Vec2d();
        get_block_world_coordinates(block_x,block_y,block_world_tl,block_world_br);

        // Convert them to local coordinates
        out_min.set( refgrid.wld2pix(block_world_tl).round() );
        out_max.set( refgrid.wld2pix(block_world_br).round() );

        // Make sure min/max relation is satisfied
        int swap_buf;
        if (out_min.x > out_max.x) {swap_buf = out_min.x; out_min.x = out_max.x; out_max.x = swap_buf;}
        if (out_min.y > out_max.y) {swap_buf = out_min.y; out_min.y = out_max.y; out_max.y = swap_buf;}
    }

    //#################################################################################################
    //#                                                                                               #
    //#                                Static methods                                                 #
    //#                                                                                               #
    //#################################################################################################

    /**
     * Calculate number of blocks (smaller segments) in segment of length len.
     * block is smaller segment
     * block is considered in segment if it intersects it
     * line spans from 0 to len
     * blocks have boundarues at offset, offset+size, offset+size*2, etc..
     * offset MUST be negative and less than size by absolute value
     * @param len length of segment
     * @param offset offset of first block (MUST BE NEGATIVE AND STRICTLY GREATER THAN -size)
     * @param size size of block
     * @return number of blocks within (or intersecting) segment of length len
     */
    private static int get_n_blocks(int len, int offset, int size) {
        // remember: bxoff is always negative
        // always prefer integer arithmetic over double
        return (len-offset)/size + ( (len-offset)%size > 0 ? 1 : 0);
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

    public static AsyncBlockGenerator from_datasets(RasterDataset[] datasets, READ_AREA read_area) {
        RasterGrid read_grid = null;
        if (read_area == READ_AREA.INTERSECTION) {

            read_grid = intersection(datasets);

            // Calculate block size to match first dataset
            int bw = datasets[0].block_width();
            int bh = datasets[0].block_height();

            // Calculate block offset to match first dataset
            Vec2i offset = read_grid.wld2pix( datasets[0].grid().pix2wld(0,0) ).round();
            int bxoff = offset.x;
            int byoff = offset.y;

            return new AsyncBlockGenerator(datasets,read_grid,bxoff,byoff,bw,bh);

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
