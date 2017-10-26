package backend.rrn;

import backend.rasterio.ImageReader;
import backend.rasterio.RasterDataset;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RRN {

    public static RRNResult calculate(RasterDataset source, RasterDataset target) {

        ImageReader reader = ImageReader.from_datasets(source,target);

        throw new NotImplementedException();

//        return new RRNResult();
    }

}
