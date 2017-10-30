package backend.rasterio;

import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.junit.Test;

import java.nio.*;

import static org.junit.Assert.*;

public class ImageWriterTest {

    public ImageWriterTest() {
        // Initialize gdal
        gdal.AllRegister();
    }

    @Test
    public void write_test1() {
        int x_size = 100;
        int y_size = 100;
        int n_bands = 3;

        byte[][] arrs = new byte[n_bands][x_size*y_size];

        // populate double array;
        for (int b = 0; b != n_bands; ++b) {
            for (int x = 0; x != x_size; ++x) {
                for (int y = 0; y != y_size; ++y) {
                    arrs[b][x + y * x_size] = (byte)( (x%( (b+1) *23))+(y%( (b+1)*19)) );
                }
            }
        }

        // Convert into array of double buffers;
        ByteBuffer[] bufs = new ByteBuffer[n_bands];
        for (int b = 0; b != n_bands; ++b) {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(x_size*y_size);
            bbuf.order(ByteOrder.nativeOrder());

            //DoubleBuffer dbuf = bbuf.asDoubleBuffer();
            //dbuf.put(arrs[b]);
            bbuf.put(arrs[b]);


            bufs[b] = bbuf;
        }

        ImageWriter.write("/home/artef/Desktop/write_test.tiff",bufs,0,0,x_size,y_size );
    }

    @Test
    public void write_test2() {
        int x_size = 100;
        int y_size = 100;
        int n_bands = 12;

        short[][] arrs = new short[n_bands][x_size*y_size];

        // populate double array;
        for (int b = 0; b != n_bands; ++b) {
            for (int x = 0; x != x_size; ++x) {
                for (int y = 0; y != y_size; ++y) {
                    arrs[b][x + y * x_size] = (short)( (x+y)*100 );
                }
            }
        }

        // Convert into array of double buffers;
        ShortBuffer[] bufs = new ShortBuffer[n_bands];
        for (int b = 0; b != n_bands; ++b) {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(x_size*y_size*2);
            bbuf.order(ByteOrder.nativeOrder());

            ShortBuffer sbuf = bbuf.asShortBuffer();
            sbuf.put(arrs[b]);

            bufs[b] = sbuf;
        }

        ImageWriter.write("/home/artef/Desktop/write_test.tiff",bufs,0,0,x_size,y_size );

    }

}