package backend.utils;

import org.gdal.gdalconst.gdalconst;

import java.nio.*;

public class TypeUtils {

    /**
     * Get size if specific gdal type in bytes
     * @param gdal_type type to get size of
     * @return
     */
    public static int get_size(int gdal_type) {
        if (gdal_type == gdalconst.GDT_Byte) return 1;
        if (gdal_type == gdalconst.GDT_UInt16) return 2;
        if (gdal_type == gdalconst.GDT_Int16) return 2;
        if (gdal_type == gdalconst.GDT_UInt32) return 4;
        if (gdal_type == gdalconst.GDT_Int32) return 4;
        if (gdal_type == gdalconst.GDT_Float32) return 4;
        if (gdal_type == gdalconst.GDT_Float64) return 8;
        throw new IllegalArgumentException();
    }

}
