package backend.utils;

import org.gdal.gdalconst.gdalconst;
import sun.nio.ch.DirectBuffer;

import java.nio.*;

public class BufferUtils {

    public static int getGdalType(Object o) {
        if (o instanceof ByteBuffer) return gdalconst.GDT_Byte;
        if (o instanceof ShortBuffer) return gdalconst.GDT_Int16;
        if (o instanceof IntBuffer) return gdalconst.GDT_Int32;
        if (o instanceof FloatBuffer) return gdalconst.GDT_Float32;
        if (o instanceof DoubleBuffer) return gdalconst.GDT_Float64;
        throw new IllegalArgumentException();
    }

    public static ByteBuffer allocateDirect( int gdal_type, int n_elems ) {

        // Get size of buffer
        int size = n_elems*TypeUtils.get_size(gdal_type);

        ByteBuffer bbuf;
        bbuf = ByteBuffer.allocateDirect(size);
        bbuf.order(ByteOrder.nativeOrder());
        return bbuf;

    }

    private static ByteBuffer allocateDirectByte(int n_elems) {
        ByteBuffer bbuf = ByteBuffer.allocateDirect(n_elems);
        bbuf.order(ByteOrder.nativeOrder());
        return bbuf;
    }

    public static double[] toDoubleArr(ByteBuffer bb, int gdal_type) {
        double[] ret = new double[bb.remaining()/TypeUtils.get_size(gdal_type)];
        if (gdal_type == gdalconst.GDT_UInt16) {
            for (int i = 0; i != ret.length; ++i) {
                ret[i] = (double)Short.toUnsignedInt(bb.getShort());
            }
            return ret;
        }
        throw new IllegalArgumentException();
    }

    public static long[] toLongArr(ByteBuffer bb, int gdal_type) {
        long[] ret = new long[bb.remaining()/TypeUtils.get_size(gdal_type)];
        if (gdal_type == gdalconst.GDT_UInt16) {
            for (int i = 0; i != ret.length; ++i) {
                ret[i] = Short.toUnsignedLong(bb.getShort());
            }
            return ret;
        }
        throw new IllegalArgumentException();
    }

    public static int[] toIntArr(ByteBuffer bb, int gdal_type) {
        int[] ret = new int[bb.remaining()/TypeUtils.get_size(gdal_type)];
        if (gdal_type == gdalconst.GDT_UInt16) {
            for (int i = 0; i != ret.length; ++i) {
                ret[i] = Short.toUnsignedInt(bb.getShort());
            }
            return ret;
        }
        throw new IllegalArgumentException();
    }

    public static short[] toShortArr(ByteBuffer bb, int gdal_type) {
        short[] ret = new short[bb.remaining()/TypeUtils.get_size(gdal_type)];
        if (gdal_type == gdalconst.GDT_UInt16) {
            throw new IllegalArgumentException("gdal type GDT_UInt16 does not convert into signed short");
        }
        if (gdal_type == gdalconst.GDT_Int16) {
            for (int i = 0; i != ret.length; ++i) {
                ret[i] = bb.getShort();
            }
            return ret;
        }
        throw new IllegalArgumentException();
    }

    private static double[] toDoubleArr(ByteBuffer bb) {
        double[] ret = new double[bb.remaining()];
        bb.asDoubleBuffer().get(ret);
        return ret;
    }

}
