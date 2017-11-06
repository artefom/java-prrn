package backend.rasterio;

import backend.utils.BufferUtils;
import org.gdal.gdal.gdal;
import org.junit.Test;

import java.io.IOException;

import static backend.utils.MathUtils.mean;
import static backend.utils.MathUtils.square_sum;
import static backend.utils.MathUtils.sum;
import static org.junit.Assert.*;

public class AsyncGdalReaderTest {

    RasterDataset ds1;
    RasterDataset ds2;
    private static final double DELTA = 1e-15;

    public AsyncGdalReaderTest() throws IOException {
        gdal.AllRegister(); // initialize gdal

        String path1 = RasterGridTest.class.getClassLoader().getResource("test_img1.tif").getPath();
        String path2 = RasterGridTest.class.getClassLoader().getResource("test_img2.tif").getPath();

        ds1 = RasterDataset.from_file(path1);
        ds2 = RasterDataset.from_file(path2);
    }


    @Test
    public void test1() throws IOException, InterruptedException {

        AsyncGdalReader reader = AsyncGdalReader.from_datasets(new RasterDataset[]{ds1,ds2});

        int block_count = 0;
        int[][] pixel_count = new int[2][reader.n_bands()];
        int pixel_count_total = 0;
        long[][] pixel_sum = new long[2][reader.n_bands()];
        long[][] pixel_square_sum = new long[2][reader.n_bands()];

        reader.start();

        BlockInfo info;

        // Calculate statistics
        while ( (info = reader.next()) != null ) {
            pixel_count_total += info.width*info.height;
            block_count += 1;

            for (int dataset_id = 0; dataset_id != info.n_datasets(); ++dataset_id) {
                for (int band_id = 0; band_id != info.n_bands(); ++band_id) {
                    // Get pixels as doubles for current band and dataset
                    int[] pixels = BufferUtils.toIntArr(
                            info.get_data(dataset_id,band_id),
                            info.get_data_type(dataset_id,band_id)
                    );
                    pixel_count[dataset_id][band_id] += pixels.length;
                    pixel_sum[dataset_id][band_id] += sum(pixels);
                    pixel_square_sum[dataset_id][band_id] += square_sum(pixels);

                }
            }
        }

        // Check number of pixels
        RasterGrid intersection = ds1.grid().intersection(ds2.grid());
        assertEquals(intersection.get_width()*intersection.get_height(),pixel_count_total);



        // Dataset 0, Band 0
        assertEquals(5542329134L,pixel_sum[0][0]);
        assertEquals(47401667496854L,pixel_square_sum[0][0]);
        assertEquals(651000,pixel_count[0][0]);

        // Dataset 1, Band 0
        assertEquals(6477422758L,pixel_sum[1][0]);
        assertEquals(64673876933318L,pixel_square_sum[1][0]);
        assertEquals(651000,pixel_count[1][0]);

        // Dataset 0, Band 1
        assertEquals(5088121271L,pixel_sum[0][1]);
        assertEquals(40041072062779L,pixel_square_sum[0][1]);
        assertEquals(651000,pixel_count[0][1]);

        // Dataset 1, Band 1
        assertEquals(5997901507L,pixel_sum[1][1]);
        assertEquals(55564124615475L,pixel_square_sum[1][1]);
        assertEquals(651000,pixel_count[1][1]);

        // Dataset 0, Band 2
        assertEquals(4705085091L,pixel_sum[0][2]);
        assertEquals(34358462063259L,pixel_square_sum[0][2]);
        assertEquals(651000,pixel_count[0][2]);

        // Dataset 1, Band 2
        assertEquals(5420538092L,pixel_sum[1][2]);
        assertEquals(45585736356346L,pixel_square_sum[1][2]);
        assertEquals(651000,pixel_count[1][2]);

        // Dataset 0, Band 3
        assertEquals(4347062119L,pixel_sum[0][3]);
        assertEquals(29512888250311L,pixel_square_sum[0][3]);
        assertEquals(651000,pixel_count[0][3]);

        // Dataset 1, Band 3
        assertEquals(5334073784L,pixel_sum[1][3]);
        assertEquals(44583514978420L,pixel_square_sum[1][3]);
        assertEquals(651000,pixel_count[1][3]);

        // Dataset 0, Band 4
        assertEquals(7165050584L,pixel_sum[0][4]);
        assertEquals(81602990857284L,pixel_square_sum[0][4]);
        assertEquals(651000,pixel_count[0][4]);

        // Dataset 1, Band 4
        assertEquals(6875487800L,pixel_sum[1][4]);
        assertEquals(74246754296180L,pixel_square_sum[1][4]);
        assertEquals(651000,pixel_count[1][4]);

        // Dataset 0, Band 5
        assertEquals(5619914287L,pixel_sum[0][5]);
        assertEquals(50264604790129L,pixel_square_sum[0][5]);
        assertEquals(651000,pixel_count[0][5]);

        // Dataset 1, Band 5
        assertEquals(6598001752L,pixel_sum[1][5]);
        assertEquals(70999713834856L,pixel_square_sum[1][5]);
        assertEquals(651000,pixel_count[1][5]);

        // Dataset 0, Band 6
        assertEquals(4527025209L,pixel_sum[0][6]);
        assertEquals(32415862654397L,pixel_square_sum[0][6]);
        assertEquals(651000,pixel_count[0][6]);

        // Dataset 1, Band 6
        assertEquals(5547853238L,pixel_sum[1][6]);
        assertEquals(49815777356180L,pixel_square_sum[1][6]);
        assertEquals(651000,pixel_count[1][6]);

        // Dataset 0, Band 7
        assertEquals(4537821368L,pixel_sum[0][7]);
        assertEquals(32054243384706L,pixel_square_sum[0][7]);
        assertEquals(651000,pixel_count[0][7]);

        // Dataset 1, Band 7
        assertEquals(5377941569L,pixel_sum[1][7]);
        assertEquals(45030413095923L,pixel_square_sum[1][7]);
        assertEquals(651000,pixel_count[1][7]);

        // Dataset 0, Band 8
        assertEquals(3274523049L,pixel_sum[0][8]);
        assertEquals(16471002564955L,pixel_square_sum[0][8]);
        assertEquals(651000,pixel_count[0][8]);

        // Dataset 1, Band 8
        assertEquals(3319372697L,pixel_sum[1][8]);
        assertEquals(16931154319461L,pixel_square_sum[1][8]);
        assertEquals(651000,pixel_count[1][8]);

        // Dataset 0, Band 9
        assertEquals(14398346764L,pixel_sum[0][9]);
        assertEquals(318543237773582L,pixel_square_sum[0][9]);
        assertEquals(651000,pixel_count[0][9]);

        // Dataset 1, Band 9
        assertEquals(14800555729L,pixel_sum[1][9]);
        assertEquals(337087462492435L,pixel_square_sum[1][9]);
        assertEquals(651000,pixel_count[1][9]);

        // Dataset 0, Band 10
        assertEquals(13423777486L,pixel_sum[0][10]);
        assertEquals(276875795697442L,pixel_square_sum[0][10]);
        assertEquals(651000,pixel_count[0][10]);

        // Dataset 1, Band 10
        assertEquals(13737709149L,pixel_sum[1][10]);
        assertEquals(290459425798427L,pixel_square_sum[1][10]);
        assertEquals(651000,pixel_count[1][10]);

        // Dataset 0, Band 11
        assertEquals(1776861440L,pixel_sum[0][11]);
        assertEquals(4851185367552L,pixel_square_sum[0][11]);
        assertEquals(651000,pixel_count[0][11]);

        // Dataset 1, Band 11
        assertEquals(1798540708L,pixel_sum[1][11]);
        assertEquals(5038868583504L,pixel_square_sum[1][11]);
        assertEquals(651000,pixel_count[1][11]);


    }

    @Test
    public void test2() throws IOException, InterruptedException {
        //complex_test(0,0,10,10);

        complex_test(-1,10,700,1,2,5);
        complex_test(4,-5,700,2,2,5);
        complex_test(-500,-1,700,3,2,5);
        complex_test(1000,-600,700,1,2,5);
        complex_test(10,-5,700,1,2,5);
        complex_test(500,10000,700,1,2,5);

    }

    private void complex_test(int bxoff, int byoff, int bw, int bh,
                              int n_threads, int buf_size) throws IOException, InterruptedException {

        AsyncGdalReader reader = AsyncGdalReader.from_datasets(new RasterDataset[]{ds1,ds2},
                bxoff,byoff,bw,bh,n_threads,buf_size);

        int total_block_count = -1;
        int block_count = 0;
        int[][] pixel_count = new int[2][reader.n_bands()];
        int pixel_count_total = 0;
        long[][] pixel_sum = new long[2][reader.n_bands()];
        long[][] pixel_square_sum = new long[2][reader.n_bands()];

        reader.start();

        BlockInfo info;

        // Calculate statistics
        while ( (info = reader.next()) != null ) {
            pixel_count_total += info.width*info.height;
            block_count += 1;

            if (total_block_count < 0) total_block_count = info.totalxblocks*info.totalyblocks;

            for (int dataset_id = 0; dataset_id != info.n_datasets(); ++dataset_id) {
                for (int band_id = 0; band_id != info.n_bands(); ++band_id) {
                    // Get pixels as doubles for current band and dataset
                    int[] pixels = BufferUtils.toIntArr(
                            info.get_data(dataset_id,band_id),
                            info.get_data_type(dataset_id,band_id)
                    );

                    // Make sure number of pixels is less than block size
                    assertTrue(pixels.length <= bw*bh );
                    assertTrue(info.width <= bw);
                    assertTrue( info.height <= bh);

                    pixel_count[dataset_id][band_id] += pixels.length;
                    pixel_sum[dataset_id][band_id] += sum(pixels);
                    pixel_square_sum[dataset_id][band_id] += square_sum(pixels);

                }
            }
        }

        // Check number of pixels
        RasterGrid intersection = ds1.grid().intersection(ds2.grid());
        assertEquals(intersection.get_width()*intersection.get_height(),pixel_count_total);

        // Make sure total number of blocks is as expected
        assertEquals(total_block_count,block_count);

        // Dataset 0, Band 0
        assertEquals(5542329134L,pixel_sum[0][0]);
        assertEquals(47401667496854L,pixel_square_sum[0][0]);
        assertEquals(651000,pixel_count[0][0]);

        // Dataset 1, Band 0
        assertEquals(6477422758L,pixel_sum[1][0]);
        assertEquals(64673876933318L,pixel_square_sum[1][0]);
        assertEquals(651000,pixel_count[1][0]);

        // Dataset 0, Band 1
        assertEquals(5088121271L,pixel_sum[0][1]);
        assertEquals(40041072062779L,pixel_square_sum[0][1]);
        assertEquals(651000,pixel_count[0][1]);

        // Dataset 1, Band 1
        assertEquals(5997901507L,pixel_sum[1][1]);
        assertEquals(55564124615475L,pixel_square_sum[1][1]);
        assertEquals(651000,pixel_count[1][1]);

        // Dataset 0, Band 2
        assertEquals(4705085091L,pixel_sum[0][2]);
        assertEquals(34358462063259L,pixel_square_sum[0][2]);
        assertEquals(651000,pixel_count[0][2]);

        // Dataset 1, Band 2
        assertEquals(5420538092L,pixel_sum[1][2]);
        assertEquals(45585736356346L,pixel_square_sum[1][2]);
        assertEquals(651000,pixel_count[1][2]);

        // Dataset 0, Band 3
        assertEquals(4347062119L,pixel_sum[0][3]);
        assertEquals(29512888250311L,pixel_square_sum[0][3]);
        assertEquals(651000,pixel_count[0][3]);

        // Dataset 1, Band 3
        assertEquals(5334073784L,pixel_sum[1][3]);
        assertEquals(44583514978420L,pixel_square_sum[1][3]);
        assertEquals(651000,pixel_count[1][3]);

        // Dataset 0, Band 4
        assertEquals(7165050584L,pixel_sum[0][4]);
        assertEquals(81602990857284L,pixel_square_sum[0][4]);
        assertEquals(651000,pixel_count[0][4]);

        // Dataset 1, Band 4
        assertEquals(6875487800L,pixel_sum[1][4]);
        assertEquals(74246754296180L,pixel_square_sum[1][4]);
        assertEquals(651000,pixel_count[1][4]);

        // Dataset 0, Band 5
        assertEquals(5619914287L,pixel_sum[0][5]);
        assertEquals(50264604790129L,pixel_square_sum[0][5]);
        assertEquals(651000,pixel_count[0][5]);

        // Dataset 1, Band 5
        assertEquals(6598001752L,pixel_sum[1][5]);
        assertEquals(70999713834856L,pixel_square_sum[1][5]);
        assertEquals(651000,pixel_count[1][5]);

        // Dataset 0, Band 6
        assertEquals(4527025209L,pixel_sum[0][6]);
        assertEquals(32415862654397L,pixel_square_sum[0][6]);
        assertEquals(651000,pixel_count[0][6]);

        // Dataset 1, Band 6
        assertEquals(5547853238L,pixel_sum[1][6]);
        assertEquals(49815777356180L,pixel_square_sum[1][6]);
        assertEquals(651000,pixel_count[1][6]);

        // Dataset 0, Band 7
        assertEquals(4537821368L,pixel_sum[0][7]);
        assertEquals(32054243384706L,pixel_square_sum[0][7]);
        assertEquals(651000,pixel_count[0][7]);

        // Dataset 1, Band 7
        assertEquals(5377941569L,pixel_sum[1][7]);
        assertEquals(45030413095923L,pixel_square_sum[1][7]);
        assertEquals(651000,pixel_count[1][7]);

        // Dataset 0, Band 8
        assertEquals(3274523049L,pixel_sum[0][8]);
        assertEquals(16471002564955L,pixel_square_sum[0][8]);
        assertEquals(651000,pixel_count[0][8]);

        // Dataset 1, Band 8
        assertEquals(3319372697L,pixel_sum[1][8]);
        assertEquals(16931154319461L,pixel_square_sum[1][8]);
        assertEquals(651000,pixel_count[1][8]);

        // Dataset 0, Band 9
        assertEquals(14398346764L,pixel_sum[0][9]);
        assertEquals(318543237773582L,pixel_square_sum[0][9]);
        assertEquals(651000,pixel_count[0][9]);

        // Dataset 1, Band 9
        assertEquals(14800555729L,pixel_sum[1][9]);
        assertEquals(337087462492435L,pixel_square_sum[1][9]);
        assertEquals(651000,pixel_count[1][9]);

        // Dataset 0, Band 10
        assertEquals(13423777486L,pixel_sum[0][10]);
        assertEquals(276875795697442L,pixel_square_sum[0][10]);
        assertEquals(651000,pixel_count[0][10]);

        // Dataset 1, Band 10
        assertEquals(13737709149L,pixel_sum[1][10]);
        assertEquals(290459425798427L,pixel_square_sum[1][10]);
        assertEquals(651000,pixel_count[1][10]);

        // Dataset 0, Band 11
        assertEquals(1776861440L,pixel_sum[0][11]);
        assertEquals(4851185367552L,pixel_square_sum[0][11]);
        assertEquals(651000,pixel_count[0][11]);

        // Dataset 1, Band 11
        assertEquals(1798540708L,pixel_sum[1][11]);
        assertEquals(5038868583504L,pixel_square_sum[1][11]);
        assertEquals(651000,pixel_count[1][11]);


    }
}