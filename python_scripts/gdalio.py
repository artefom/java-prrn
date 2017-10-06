import gdal
from gdal import gdalconst
import numpy as np

# Mappings between numpy datatypes and GDAL datatypes.
# Note that ambiguities are resolved by the order - the first one found 
# is the one chosen. 
dataTypeMapping = [
    (np.uint8,gdalconst.GDT_Byte),
    (np.bool,gdalconst.GDT_Byte),
    (np.int16,gdalconst.GDT_Int16),
    (np.uint16,gdalconst.GDT_UInt16),
    (np.int32,gdalconst.GDT_Int32),
    (np.uint32,gdalconst.GDT_UInt32),
    (np.single,gdalconst.GDT_Float32),
    (np.float,gdalconst.GDT_Float64)
]

def GDALTypeToNumpyType(gdaltype):
    """
    Given a gdal data type returns the matching
    numpy data type
    """
    for (numpy_type,test_gdal_type) in dataTypeMapping:
        if test_gdal_type == gdaltype:
            return numpy_type
    raise rioserrors.TypeConversionError("Unknown GDAL datatype: %s"%gdaltype)

def NumpyTypeToGDALType(numpytype):
    """
    For a given numpy data type returns the matching
    GDAL data type
    """
    for (test_numpy_type,gdaltype) in dataTypeMapping:
        if test_numpy_type == numpytype:
            return gdaltype
    raise rioserrors.TypeConversionError("Unknown numpy datatype: %s"%numpytype)

def readAsArray(ds : "gdal dataset"):
    
    # data type is determined by first band
    datatype = GDALTypeToNumpyType( ds.GetRasterBand(1).DataType )
    result = np.zeros((ds.RasterYSize,ds.RasterXSize,ds.RasterCount))
    
    for i in range( ds.RasterCount ):
        b_num = i+1
        b = ds.GetRasterBand(b_num)
        b_arr = b.ReadAsArray(0,0)
        result[:,:,i] = b_arr
        
    return result