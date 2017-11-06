package backend.rasterio;

import backend.utils.BufferUtils;
import backend.utils.Vec2d;
import backend.utils.Vec2i;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import sun.awt.SunToolkit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Class that will read from gdal dataset from parallel thread
 * and push result into queue or push null to queue as poison pill
 */
class BlockProducer implements Runnable {

    public static final BlockInfo POISON_PILL = new BlockInfo();

    private static Logger log = Logger.getLogger(BlockProducer.class.getName());

    private final RasterDataset[] datasets;
    private final AsyncBlockGenerator block_gen;
    private final BlockingQueue<BlockInfo> sink;

    // Number of bands to read
    public final int n_bands;

    public BlockProducer(String[] i_files, AsyncBlockGenerator i_block_gen, BlockingQueue<BlockInfo> i_sink)
            throws IOException {

        int n_bands_min = -1;

        datasets = new RasterDataset[i_files.length];
        for (int i = 0; i != i_files.length; ++i) {
            datasets[i] = RasterDataset.from_file(i_files[i]);

            if (datasets[i].dataset().GetRasterCount() < n_bands_min || n_bands_min == -1) {
                n_bands_min = datasets[i].dataset().GetRasterCount();
            }
        }

        n_bands = n_bands_min;
        block_gen = i_block_gen;
        sink = i_sink;

    }

    private BlockInfo read_block(int block_id) {
        BlockInfo info = new BlockInfo();

        info.totalxblocks = block_gen.get_n_horizontal_blocks();
        info.totalyblocks = block_gen.get_n_vertical_blocks();

        // Get block id
        info.block_id = block_id;

        // Get xy index of block
        Vec2i xy_index = new Vec2i();
        block_gen.get_block_xyindex(block_id,xy_index);
        info.block_x = xy_index.x;
        info.block_y = xy_index.y;

        // Make copy to make sure we don't mess grid from block_gen
        info.grid = block_gen.get_grid().clone();

        // Get World coordinates of current block
        info.world_tl = new Vec2d();
        info.world_br = new Vec2d();
        block_gen.get_block_world_coordinates(block_id,info.world_tl,info.world_br);

        // Get pixel coordinates of current block
        info.local_tl = new Vec2i();
        info.local_br = new Vec2i();
        block_gen.get_block_pixel_coordinates(info.grid,block_id,info.local_tl,info.local_br);

        // Calculate width and height of current block
        info.width  = info.local_br.x-info.local_tl.x;
        info.height = info.local_br.y-info.local_tl.y;

        // Assign datasets to block info. Not sure if it's safe cus datasets
        // are from different thread, so skip it right now
//        info.datasets = new Dataset[datasets.length];
//        for (int i = 0; i != datasets.length; ++i) {
//            info.datasets[i] = datasets[i].dataset();
//        }

        // The most difficult part - contents

        // Variables to store area to read
        Vec2i ds_tl = new Vec2i();
        Vec2i ds_br = new Vec2i();


        info.set_datasets_info(datasets.length,n_bands);


        for (int dataset_id = 0; dataset_id != datasets.length; ++dataset_id) {
            Dataset ds = datasets[dataset_id].dataset();
            RasterGrid grid = datasets[dataset_id].grid();

            // Get area to read
            block_gen.get_block_pixel_coordinates(grid,block_id,ds_tl,ds_br);
            int xoff = ds_tl.x;
            int yoff = ds_tl.y;
            int xsize = ds_br.x - ds_tl.x;
            int ysize = ds_br.y - ds_tl.y;

            for (int band_id = 0; band_id != n_bands; ++band_id) {
                // Get current band
                Band band = ds.GetRasterBand(band_id+1);

                // get data type
                int data_type = band.GetRasterDataType();

                // Read data
                ByteBuffer bb = BufferUtils.allocateDirect(data_type,xsize*ysize);
                band.ReadRaster_Direct(xoff,yoff,xsize,ysize,xsize,ysize,data_type,bb,0,0);

                // Put data info BlockInfo
                info.set_data(dataset_id,band_id,bb);
                info.set_data_type(dataset_id,band_id,data_type);
            }
        }

        return info;
    }

    @Override
    public void run() {

        try {

            int block_n;
            while ( (block_n = block_gen.get_next_block_id()) >= 0 ) {

                BlockInfo bi = read_block(block_n);

                // Put current block into sink
                sink.put(bi);
                Thread.yield();
            }

            // Put poison pill to indicate that current thread finished it's job
            sink.put(POISON_PILL);

            // Delete all datasets
            for (int i = 0; i != datasets.length; ++i) {
                datasets[i].delete();
            }

        } catch (InterruptedException ex) {
            // This should never happen!
            throw new RuntimeException("Interrupted!");
        }

    }

    /**
     * Read block of data from multiple raster bands into single byte buffer
     * @param ds
     * @param xoff
     * @param yoff
     * @param xsize
     * @param ysize
     * @return ByteBuffer[] each element contains bytes for specific band first element for first band,
     * second for second, etc.
     */
    private static ByteBuffer[] read_block(Dataset ds, int xoff, int yoff, int xsize, int ysize) {
        int n_layers = ds.getRasterCount();

        ByteBuffer[] ret = new ByteBuffer[n_layers];

        for (int layer_n = 0; layer_n != ds.getRasterCount(); ++layer_n) {
            Band b = ds.GetRasterBand(layer_n+1);
            int type = b.getDataType(); // Get data type for current band
            ByteBuffer bb = BufferUtils.allocateDirect(type,xsize*ysize);
            b.ReadRaster_Direct(xoff,yoff,xsize,ysize,xsize,ysize,type,bb,0,0);
            ret[layer_n] = bb;
        }

        return ret;
    }

}

/**
 * Reads gdal file in async manner
 * Block by block, using {@link AsyncBlockGenerator}
 * Block are selected to match file block position
 *
 * This class Spawns multiple threads internally
 * and request data from storage.
 * it returns first block that was read successfully
 *
 * This is next lvl of ImageReader
 */
public class AsyncGdalReader {

    // Storage for datasets
    private final RasterDataset[] datasets;

    // Class for generating sequence of areas to read from each file
    private final AsyncBlockGenerator block_gen;

    // Producers
    private final BlockProducer[] producers;

    // Threads
    private final Thread[] threads;

    // Queue
    private final BlockingQueue<BlockInfo> queue;

    // Number of running threads
    // This value decreases every time next() recieves a null from queue
    // If n_working_threads == 0 and queue is empty, job finished
    // Poison pill were chosen over synchronized methods for simplicity
    private int n_working_threads;

    // Number of bands to read
    // This value is calculated on initialization by each BlockProducer
    // Just pull it here
    private final int n_bands;

    /**
     * Constructior. creates AsyncGdalReader from datasets
     * @param i_datasets datasets to read from
     * @param n_threads number of threads to read
     * @param buf_size block buffer size
     */
    public AsyncGdalReader(RasterDataset[] i_datasets, AsyncBlockGenerator i_block_gen, int n_threads, int buf_size) throws IOException {

        producers = new BlockProducer[n_threads];
        threads = new Thread[n_threads];

        queue = new ArrayBlockingQueue<BlockInfo>(buf_size);

        // Storage for datasets
        datasets = i_datasets;

        // Class for generating sequence of areas to read from each file
        block_gen = i_block_gen;

        // Get filenames to pass to prodicers
        String[] filenames = new String[datasets.length];
        for (int i = 0; i != filenames.length; ++i) {
            filenames[i] = datasets[i].get_filename();
        }

        // Spawn producers
        for (int i = 0; i != producers.length; ++i) {
            producers[i] = new BlockProducer(filenames,block_gen,queue);
        }

        // Get number of bands
        n_bands = producers[0].n_bands;

        // Set number of working threads to -1
        // To indicate that we have not started yet
        n_working_threads = -1;
    }

    //###############################################################################################
    //#                                                                                             #
    //#                          Approved methods                                                   #
    //#                                                                                             #
    //###############################################################################################

    /**
     * Get next block
     * Async method
     * Must call {@link #start()} before this one
     * before calling this method
     * @return file, containing pixel values from opened datasets or null of no more data to read.
     */
    public synchronized BlockInfo next() throws InterruptedException {

        BlockInfo val;

        do {
            if (n_working_threads <= 0 && queue.size() == 0) return null; // Finished processing, return poison

            // seems like some threads are working, or queue is not empty!
            val = queue.take();
            if (val == BlockProducer.POISON_PILL) { // repeat until we get an actual value
                n_working_threads -= 1;
            } else {
                break;
            }

        } while (true);

        return val;
    }

    public int n_datasets() {
        return datasets.length;
    }

    public int n_bands() {
        return n_bands;
    }

    /**
     * Starts reading data from disc and storing it in buffer
     * until demanded
     */
    public void start() {
        if (n_working_threads != -1) throw new IllegalThreadStateException();
        // Spawn and run threds
        for (int i = 0; i != threads.length; ++i) {
            threads[i] = new Thread(producers[i]);
            threads[i].start();
        }

        // Set number of working threads
        n_working_threads = threads.length;
    }

    //########################## Private methods ####################################################

    /**
     * Check if any threads are busy at reading
     * @return true if any threads are busy
     */
    private boolean threads_runnin() {
        throw new NotImplementedException();
    }

    //########################## STATIC METHODS #####################################################

    // creation with custom block size
    static AsyncGdalReader from_datasets(RasterDataset[] ds,
                                         int block_offset_x, int block_offset_y,
                                         int block_width, int block_height, int n_threads, int buf_size) throws IOException {

        AsyncBlockGenerator block_gen = AsyncBlockGenerator.from_datasets(ds,
                block_offset_x,block_offset_y,block_width,block_height,
                AsyncBlockGenerator.READ_AREA.INTERSECTION);

        AsyncGdalReader ret = new AsyncGdalReader(ds,block_gen,n_threads,buf_size);
        return ret;
    }

    // creation
    static AsyncGdalReader from_datasets(RasterDataset[] ds) throws IOException {

        AsyncBlockGenerator block_gen = AsyncBlockGenerator.from_datasets(ds,
                AsyncBlockGenerator.READ_AREA.INTERSECTION);

        AsyncGdalReader ret = new AsyncGdalReader(ds,block_gen,2,20);
        return ret;
    }

    //###############################################################################################
    //#                                                                                             #
    //#                          Purposed methods                                                   #
    //#                                                                                             #
    //###############################################################################################
//
//    /**
//     * Acquires an exclusive lock on this channel's file.
//     * @return
//     */
//    public Future<FileLock> lock() {
//        throw new NotImplementedException();
//    };
//
//    /**
//     * Acquires an exclusive lock on this channel's file.
//     * @param handler
//     * @param <A>
//     */
//    public <A> void lock(A attachment, CompletionHandler<FileLock, ? super A> handler) {
//        throw new NotImplementedException();
//    }
//
//    /**
//     * Acquires a lock on the given region of this channel's file.
//     * @param position
//     * @param size
//     * @param shared
//     * @return
//     */
//    public Future<FileLock> lock(long position, long size, boolean shared) {
//        throw new NotImplementedException();
//    }
//
//    public <A> void lock(long position, long size, boolean shared,
//                         A attachment, CompletionHandler<FileLock,? super A> handler) {
//        throw new NotImplementedException();
//    }
//
//    static AsyncGdalReader open(Path[] files, int type) {
//        if (type == gdalconstConstants.GA_ReadOnly) {
//            throw new NotImplementedException();
//        }
//        throw  new NotImplementedException();
//    }
//
//
//    public Future<Integer> read() {
//        throw new NotImplementedException();
//    }
//
//    public Future<Integer> write() {
//        throw new NotImplementedException();
//    }
}
