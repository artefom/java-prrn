package backend.rasterio;

import backend.utils.Vec2d;
import backend.utils.Vec2i;
import org.gdal.gdal.gdal;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AsyncBlockGeneratorTest {

    RasterDataset ds1;
    RasterDataset ds2;
    private static final double DELTA = 1e-15;

    public AsyncBlockGeneratorTest() throws IOException {
        gdal.AllRegister(); // initialize gdal

        String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
        String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

        ds1 = RasterDataset.from_file(path1);
        ds2 = RasterDataset.from_file(path2);
    }

    @Test
    public void test1() {

        AsyncBlockGenerator gen = AsyncBlockGenerator.from_datasets( new RasterDataset[]{ds1,ds2},
                AsyncBlockGenerator.READ_AREA.INTERSECTION );

        assertEquals(930, gen.get_n_blocks() );
        assertEquals(1,gen.get_n_horizontal_blocks());
        assertEquals(930,gen.get_n_vertical_blocks());
        assertEquals(2,gen.get_n_datasets());

        int[] scaned_blocks = new int[gen.get_n_blocks()];
        for (int i = 0; i != scaned_blocks.length; ++i) scaned_blocks[i] = 0;

        // Scan all blocks
        int block_n;
        while ( (block_n = gen.get_next_block_id()) >= 0 ) {
            assertTrue(block_n < 930 );
            scaned_blocks[block_n] += 1;
        }

        // Make sure we have visited each block at least and only once
        for (int i = 0; i != scaned_blocks.length; ++i) {
            assertEquals(1,scaned_blocks[i]);
        }
    }

    @Test
    public void test2() {
        AsyncBlockGenerator gen = AsyncBlockGenerator.from_datasets( new RasterDataset[]{ds1,ds2},
                AsyncBlockGenerator.READ_AREA.INTERSECTION );

        RasterGrid intersection = ds1.grid().intersection(ds2.grid());

        // Scan first block
        Vec2d world_coords_min = new Vec2d(-1,-1);
        Vec2d world_coords_max = new Vec2d(-1,-1);
        gen.get_block_world_coordinates(0, world_coords_min, world_coords_max);

        assertEquals(world_coords_min.x,intersection.xMin,DELTA);
        assertEquals(world_coords_max.x,intersection.xMax,DELTA);
        assertEquals(world_coords_max.y,intersection.yMax,DELTA);

        assertEquals(world_coords_max.x,world_coords_min.x+intersection.yRes*700,DELTA);
        assertEquals(world_coords_max.y,world_coords_min.y+intersection.yRes*1,DELTA);

        int block_n;
        while ( (block_n = gen.get_next_block_id()) >= 0 ) {
            gen.get_block_world_coordinates(block_n, world_coords_min, world_coords_max);
            assertEquals(world_coords_min.x,intersection.xMin,DELTA);
            assertEquals(world_coords_max.x,intersection.xMax,DELTA);
            assertEquals(world_coords_max.y,intersection.yMax-intersection.yRes*block_n,DELTA);
        }
    }

    /**
     * More complicated test
     */
    @Test
    public void test3() {
        RasterDataset[] datasets = new RasterDataset[]{ds1,ds2};
        RasterGrid intersection = ds1.grid().intersection(ds2.grid());
        int block_width = 100;
        int block_height = 100;
        AsyncBlockGenerator gen = new AsyncBlockGenerator(datasets,intersection,0,0,block_width,block_height);

        assertEquals( 7, gen.get_n_horizontal_blocks() );
        assertEquals( 10, gen.get_n_vertical_blocks() );

        test_bg(0,0,100,100);
    }

    /**
     * Even more complicated test
     */
    @Test
    public void test4() {
        test_bg(1,1,100,100);
        test_bg(1,-1,100,100);
        test_bg(-1,1,100,100);
        test_bg(-100,-100,100,100);
        test_bg(-101,-101,100,100);
        test_bg(99,99,100,100);
        test_bg(100,100,100,100);
        test_bg(101,101,100,100);

        test_bg(0,0,13,17);
        test_bg(1,1,13,17);
        test_bg(-1,-1,13,17);
        test_bg(-1,1,13,17);
        test_bg(1,-1,13,17);
        test_bg(5,-1,13,17);
        test_bg(1,5,13,17);
        test_bg(5,1,13,17);
        test_bg(5,-5,13,17);
    }

    private void test_bg(int xoff, int yoff, int xsize, int ysize) {
        RasterDataset[] datasets = new RasterDataset[]{ds1,ds2};
        RasterGrid intersection = ds1.grid().intersection(ds2.grid());
        Vec2i out_min = new Vec2i();
        Vec2i out_max = new Vec2i();
        int test_grid_width = intersection.get_width();
        int test_grid_height = intersection.get_height();

        AsyncBlockGenerator gen = new AsyncBlockGenerator(datasets,intersection,xoff,yoff,xsize,ysize);

        // Initialize test grid and fill it with ones. Check later
        int[] test_grid = new int[test_grid_width*test_grid_height];

        int block_count = 0;
        int block_n;
        while ( (block_n = gen.get_next_block_id()) >= 0 ) {
            gen.get_block_pixel_coordinates(intersection,block_n,out_min,out_max);

            int cur_width = out_max.x-out_min.x;
            int cur_height = out_max.y-out_min.y;
            block_count += 1;

            assertTrue(cur_width <= xsize);
            assertTrue(cur_height <= ysize);
            assertTrue( cur_width > 0);
            assertTrue( cur_height > 0);

            // Fill test area
            for (int x = out_min.x; x < out_max.x; ++x) {
                for (int y = out_min.y; y < out_max.y; ++y) {
                    assertTrue(x>=0);
                    assertTrue(x<test_grid_width);
                    assertTrue(y>=0);
                    assertTrue(y<test_grid_height);
                    test_grid[x+y*test_grid_width] += 1;
                }
            }
        }

        // Check that exactly all values of test_grid are 1
        for (int i = 0; i != test_grid.length; ++i) {
            assertEquals(1,test_grid[i]);
        }

        assertEquals(gen.get_n_horizontal_blocks()*gen.get_n_vertical_blocks(), block_count);
        assertEquals(gen.get_n_blocks(), block_count);
    }

    @Test
    public void calc_block_num() throws Exception {
        assertEquals(9, AsyncBlockGenerator.calc_block_num(2,2,2,1,6,4) );
        assertEquals(1, AsyncBlockGenerator.calc_block_num(3,3,4,1,2,2) );
        assertEquals(1, AsyncBlockGenerator.calc_block_num(3,3,2,2,1,1) );
        assertEquals(1, AsyncBlockGenerator.calc_block_num(3,3,2,2,1,1) );
        assertEquals(2, AsyncBlockGenerator.calc_block_num(3,3,2,2,2,1) );
        assertEquals(4, AsyncBlockGenerator.calc_block_num(3,3,2,2,2,2) );
        assertEquals(1, AsyncBlockGenerator.calc_block_num(3,3,0,0,1,1) );
        assertEquals(9, AsyncBlockGenerator.calc_block_num(3,3,5,2,6,5) );
        assertEquals(9, AsyncBlockGenerator.calc_block_num(3,3,5,2,7,5) );
        assertEquals(6, AsyncBlockGenerator.calc_block_num(3,3,5,2,6,4) );
        assertEquals(12, AsyncBlockGenerator.calc_block_num(3,3,5,2,8,5) );

    }

}