package backend.rasterio;

import backend.utils.Vec2i;
import org.gdal.gdal.gdal;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class BlockGeneratorTest {

    RasterDataset ds1;
    RasterDataset ds2;

    public BlockGeneratorTest() throws IOException {
        gdal.AllRegister(); // initialize gdal

        String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
        String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

        ds1 = RasterDataset.from_file(path1);
        ds2 = RasterDataset.from_file(path2);
    }

//    @Test
//    public void get_n_blocks() throws Exception {
//        // Create block generator from intersection of 2 images
//        BlockGenerator bgen = BlockGenerator.from_datasets(
//                new RasterDataset[]{ds1,ds2},BlockGenerator.READ_AREA.INTERSECTION);
//
//        System.out.println("X blocks: "+bgen.get_n_blocks_width());
//        System.out.println("Y blocks: "+bgen.get_n_blocks_height());
//        System.out.println("Total blocks: "+bgen.get_n_blocks());
//
//        Vec2i block_min = new Vec2i();
//        Vec2i block_max = new Vec2i();
//
//        bgen.get_block_world_extents(0,0,0,block_min,block_max);
//        System.out.println("DS1 Block 0 extents: "+block_min.toString()+" - "+block_max.toString());
//
//        bgen.get_block_world_extents(0,0,1,block_min,block_max);
//        System.out.println("DS1 Block 1 extents: "+block_min.toString()+" - "+block_max.toString());
//
//        bgen.get_block_world_extents(1,0,0,block_min,block_max);
//        System.out.println("DS2 Block 0 extents: "+block_min.toString()+" - "+block_max.toString());
//
//        bgen.get_block_world_extents(1,0,1,block_min,block_max);
//        System.out.println("DS2 Block 1 extents: "+block_min.toString()+" - "+block_max.toString());
//    }

    @Test
    public void calc_block_num() throws Exception {
        assertEquals(9, BlockGenerator.calc_block_num(2,2,2,1,6,4) );
        assertEquals(1, BlockGenerator.calc_block_num(3,3,4,1,2,2) );
        assertEquals(1, BlockGenerator.calc_block_num(3,3,2,2,1,1) );
        assertEquals(1, BlockGenerator.calc_block_num(3,3,2,2,1,1) );
        assertEquals(2, BlockGenerator.calc_block_num(3,3,2,2,2,1) );
        assertEquals(4, BlockGenerator.calc_block_num(3,3,2,2,2,2) );
        assertEquals(1, BlockGenerator.calc_block_num(3,3,0,0,1,1) );
        assertEquals(9, BlockGenerator.calc_block_num(3,3,5,2,6,5) );
        assertEquals(9, BlockGenerator.calc_block_num(3,3,5,2,7,5) );
        assertEquals(6, BlockGenerator.calc_block_num(3,3,5,2,6,4) );
        assertEquals(12, BlockGenerator.calc_block_num(3,3,5,2,8,5) );

    }

}