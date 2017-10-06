#!/usr/bin/env python3

# File for generating test samples from satellite images
# Use read_common_area function to read common area from files (no reprojecting done, files must be aligned)

import gdal
import pixelgrid
import csv
import numpy as np

def wld2pix(transform,geox,geoy):
    """converts a set of map coords to pixel coords"""
    x = ( transform[0] * transform[5] - 
        transform[2] * transform[3] + transform[2] * geoy - 
        transform[5] * geox ) / ( transform[2] * transform[4] - transform[1] * transform[5] )

    y = ( transform[1] * transform[3] - transform[0] * transform[4] -
        transform[1] * geoy + transform[4] * geox ) / ( transform[2] * transform[4] - transform[1] * transform[5] )

    return (x,y)

def pix2wld(transform,x,y):
    """converts a set of pixels coords to map coords"""
    geox = transform[0] + transform[1] * x + transform[2] * y
    geoy = transform[3] + transform[4] * x + transform[5] * y

    return (geox,geoy)

def get_common_area(pg1,pg2):

    wg = pg1.intersection(pg2)
    wg_gt = wg.makeGeoTransform()
    

    tl = pix2wld(wg_gt,0,0)
    br = pix2wld(wg_gt,wg.getDimensions()[1],wg.getDimensions()[0])
    
    return tl,br

def read_world_coords(f,pg,tl,br):
    ds = gdal.Open(f)
    px_tl = wld2pix(pg.makeGeoTransform(),*tl)
    px_br = wld2pix(pg.makeGeoTransform(),*br)
    
    px_tl = (round(px_tl[0]),round(px_tl[1]))
    px_br = (round(px_br[0]),round(px_br[1]))
        
    bands = []
    band_types = []
    for i in range( ds.RasterCount ):
        b_num = i+1
        b = ds.GetRasterBand(b_num)
        b_arr = b.ReadAsArray(
              px_tl[0],
              px_tl[1],
              px_br[0]-px_tl[0],
              px_br[1]-px_tl[1])
        bands.append(b_arr)
        band_types.append(b.DataType)
        
    bands = np.array(bands)
    band_types = np.array(band_types)

    bands = np.swapaxes( np.swapaxes(bands,0,1), 1,2 )
    return bands,band_types

def read_common_area(f1,f2):
    
    pg1 = pixelgrid.pixelGridFromFile(f1)
    pg2 = pixelgrid.pixelGridFromFile(f2)
    
    tl,br = get_common_area(pg1,pg2)
    
    a1,band_types1 = read_world_coords(f1,pg1,tl,br)
    a2,band_types2 = read_world_coords(f2,pg2,tl,br)
    
    xMin = min(tl[0],br[0])
    xMax = max(tl[0],br[0])
    yMin = min(tl[1],br[1])
    yMax = max(tl[1],br[1])
    xRes = pg1.xRes
    yRes = pg1.yRes
    proj = pg1.projection
    
    if pg2.xRes != xRes or pg2.yRes != yRes or pg2.projection != proj:
        raise ValueError("Something went wrong")
    
    a12_pg = pixelgrid.PixelGridDefn(xMin=xMin,xMax=xMax,yMin=yMin,yMax=yMax,xRes=xRes,yRes=yRes,projection=proj)
    
    return a1,a2,a12_pg,band_types1,band_types2

def clip_nodata(a1,a2,pg,band_types1,band_types2,nodata_value=0):
    y_profile = ((a1!=0).min(axis=2).max(axis=1))&((a2!=0).min(axis=2).max(axis=1))
    x_profile = ((a1!=0).min(axis=2).max(axis=0))&((a2!=0).min(axis=2).max(axis=0))

    y_beg = y_profile.argmax()
    y_end = y_profile.size-y_profile[::-1].argmax()
    x_beg = x_profile.argmax()
    x_end = x_profile.size-x_profile[::-1].argmax()
    
    return a1[y_beg:y_end,x_beg:x_end,:],a2[y_beg:y_end,x_beg:x_end,:],pg,band_types1,band_types2

def calc_diff(f1,f2):
    a1,a2,pg,bt1,bt2 = clip_nodata(*read_common_area(f1,f2))
    diff = np.abs(a1.astype(np.float64)-a2.astype(np.float64))
    return np.median(diff[(a1!=0)&(a2!=0)])

class TestSampleGen:
    
    def __init__(self,diffs : "[ (file1, file2, difference), (f1,f2,diff), ... ]"):
        
        self.diffs = diffs
        
    def read_sample(self,samplen):
        """
        Reads specific sample by it's id
        """
        f1 = self.diffs[samplen][0]
        f2 = self.diffs[samplen][1]
        
        return clip_nodata( *read_common_area(f1,f2) ),f1,f2
    
    def most_alike(self,n=None):
        """
        generator of most alike items
        """
        if n is None or n > len(self.diffs):
            n = len(self.diffs)
        
        for i in range(n):
            yield self.read_sample(i)
    
def TestSampleGen_from_csv(path_to_file,sort=True,lower_cutoff=100,higher_cutoff=2000):
    
    loaded_data = []
    with open(path_to_file,'r') as f:
        lines = f.readlines()
        try:
            row = next( csv.reader(lines[:1]) )
            loaded_data.append( (row[0],row[1],float(row[2])) )
        except ValueError as ex:
            pass

        for row in csv.reader(lines[1:]):
            try:
                loaded_data.append( (row[0],row[1],float(row[2])) )
            except IndexError as ex:
                loaded_data.append( (row[0],row[1],float('nan')) )
                
    loaded_data=np.array( sorted(loaded_data,key=lambda x: x[2]) )
    loaded_data = loaded_data[ ~np.isnan(loaded_data[:,2].astype(float)) ]
    loaded_data = loaded_data[ (loaded_data[:,2].astype(float) > lower_cutoff) &\
                              (loaded_data[:,2].astype(float) < higher_cutoff) ]
    
    return TestSampleGen(loaded_data)