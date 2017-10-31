package backend.rasterio;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconstConstants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.Future;

/**
 * Reads gdal file in async manner
 */
public class AsyncGdalReader {

    /**
     * Acquires an exclusive lock on this channel's file.
     * @return
     */
    public Future<FileLock> lock() {
        throw new NotImplementedException();
    };

    /**
     * Acquires an exclusive lock on this channel's file.
     * @param handler
     * @param <A>
     */
    public <A> void lock(A attachment, CompletionHandler<FileLock, ? super A> handler) {
        throw new NotImplementedException();
    }

    /**
     * Acquires a lock on the given region of this channel's file.
     * @param position
     * @param size
     * @param shared
     * @return
     */
    public Future<FileLock> lock(long position, long size, boolean shared) {
        throw new NotImplementedException();
    }

    public <A> void lock(long position, long size, boolean shared,
                         A attachment, CompletionHandler<FileLock,? super A> handler) {
        throw new NotImplementedException();
    }

    static AsyncGdalReader open(Path[] files, int type) {
        if (type == gdalconstConstants.GA_ReadOnly) {
            throw new NotImplementedException();
        }
        throw  new NotImplementedException();
    }

    static AsyncGdalReader wrap(Dataset[] ds) {
        throw new NotImplementedException();
    };

    static AsyncGdalReader wrap(RasterDataset[] ds) {
        throw new NotImplementedException();
    };

    public Future<Integer> read() {
        throw new NotImplementedException();
    }

    public Future<Integer> write() {
        throw new NotImplementedException();
    }
}
