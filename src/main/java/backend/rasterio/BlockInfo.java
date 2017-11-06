package backend.rasterio;

import backend.utils.Vec2d;
import backend.utils.Vec2i;
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
    private ByteBuffer[] data;
    private Dataset[] datasets;

    private int[] data_types; // gdal data types for each dataset and band

    private int n_datasets;
    private int n_bands;

    public RasterGrid grid;

    public int block_id;
    public int block_x;
    public int block_y;

    public int width;
    public int height;
    public int totalxblocks;
    public int totalyblocks;

    public Vec2d world_tl;
    public Vec2d world_br;

    public Vec2i local_tl;
    public Vec2i local_br;

    public void set_datasets_info(int n_datasets, int n_bands) {


        this.n_bands = n_bands;
        this.n_datasets = n_datasets;
        data = new ByteBuffer[n_datasets*n_bands];
        data_types = new int[n_datasets*n_bands];
        datasets = new Dataset[n_datasets];
    }

    public void set_data_type(int dataset_id, int band_id, int data_type) {

        //Checks for overflow
        if (dataset_id < 0)           throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" cannot be below 0!");
        if (dataset_id >= n_datasets) throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" exceeds number of datasets: "+n_datasets);
        if (band_id < 0)        throw new IndexOutOfBoundsException("Band id "+band_id+" cannot be below 0!");
        if (band_id >= n_bands) throw new IndexOutOfBoundsException("Band id "+band_id+" exceeds number of bands: "+n_bands);


        data_types[band_id+dataset_id*n_bands] = data_type;
    }

    public void set_dataset(int dataset_id, Dataset ds) {

        //Checks for overflow
        if (dataset_id < 0)           throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" cannot be below 0!");
        if (dataset_id >= n_datasets) throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" exceeds number of datasets: "+n_datasets);

        datasets[dataset_id] = ds;
    }

    public Dataset get_dataset(int dataset_id) {

        //Checks for overflow
        if (dataset_id < 0)           throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" cannot be below 0!");
        if (dataset_id >= n_datasets) throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" exceeds number of datasets: "+n_datasets);

        return datasets[dataset_id];
    }

    public int get_data_type(int dataset_id, int band_id) {

        //Checks for overflow
        if (dataset_id < 0)           throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" cannot be below 0!");
        if (dataset_id >= n_datasets) throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" exceeds number of datasets: "+n_datasets);
        if (band_id < 0)        throw new IndexOutOfBoundsException("Band id "+band_id+" cannot be below 0!");
        if (band_id >= n_bands) throw new IndexOutOfBoundsException("Band id "+band_id+" exceeds number of bands: "+n_bands);


        return data_types[band_id+dataset_id*n_bands];
    }

    public void set_data(int dataset_id, int band_id, ByteBuffer data) {

        //Checks for overflow
        if (dataset_id < 0)           throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" cannot be below 0!");
        if (dataset_id >= n_datasets) throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" exceeds number of datasets: "+n_datasets);
        if (band_id < 0)        throw new IndexOutOfBoundsException("Band id "+band_id+" cannot be below 0!");
        if (band_id >= n_bands) throw new IndexOutOfBoundsException("Band id "+band_id+" exceeds number of bands: "+n_bands);


        this.data[band_id+dataset_id*n_bands] = data;
    }

    public ByteBuffer get_data(int dataset_id, int band_id) {

        //Checks for overflow
        if (dataset_id < 0)           throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" cannot be below 0!");
        if (dataset_id >= n_datasets) throw new IndexOutOfBoundsException("Dataset id "+dataset_id+" exceeds number of datasets: "+n_datasets);
        if (band_id < 0)        throw new IndexOutOfBoundsException("Band id "+band_id+" cannot be below 0!");
        if (band_id >= n_bands) throw new IndexOutOfBoundsException("Band id "+band_id+" exceeds number of bands: "+n_bands);

        return this.data[band_id+dataset_id*n_bands];
    }

    public int n_datasets() {
        return n_datasets;
    }

    public int n_bands() {
        return n_bands;
    }
}
