package backend.rasterio;

import org.gdal.gdal.*;
import org.gdal.gdalconst.gdalconst;
import sun.nio.ch.DirectBuffer;

import javax.xml.crypto.Data;
import java.nio.*;
import java.util.HashMap;

public class ImageWriter {


    public static void write(Dataset ds, ByteBuffer[] buf, int xoff, int yoff, int xsize, int ysize, int type) {
        for (int b = 0; b != buf.length; ++b)
            ds.GetRasterBand(b+1).WriteRaster_Direct(xoff, yoff, xsize, ysize, xsize, ysize, type , buf[b]);
        ds.FlushCache();
    }

    public static void write(String filename, Object[] buf, int xoff, int yoff, int xsize, int ysize ) {

        // Mapping from buffer type to gdal type
        int type = 0;
        if (buf[0] instanceof ByteBuffer) type = gdalconst.GDT_Byte;
        else if (buf[0] instanceof ShortBuffer) type = gdalconst.GDT_Int16;
        else if (buf[0] instanceof IntBuffer) type = gdalconst.GDT_Int32;
        else if (buf[0] instanceof FloatBuffer) type = gdalconst.GDT_Float32;
        else if (buf[0] instanceof DoubleBuffer) type = gdalconst.GDT_Float64;
        else throw new IllegalArgumentException("buf must be one of: ShortBuffer[], IntBuffer[], FloatBuffer[], DoubleBuffer[]");

        Driver driver = gdal.GetDriverByName("GTiff");
        Dataset dataset = driver.Create(filename, xsize, ysize, buf.length, type);
        ByteBuffer[] bbufs = new ByteBuffer[buf.length];
        for (int b = 0; b != buf.length; ++b) {
            if (buf[b] instanceof ByteBuffer) {
                bbufs[b] = (ByteBuffer)buf[b];
            } else {
                ByteBuffer bbuf = (ByteBuffer) (((DirectBuffer) (buf[b])).attachment());
                bbufs[b] = bbuf;
            }
        }
        write(dataset,bbufs,xoff,yoff,xsize,ysize,type);
    }

}
