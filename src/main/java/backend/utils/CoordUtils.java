package backend.utils;

public class CoordUtils {


    /**
     * Convert map coords to pixel coords
     */
    public static Vec2d wld2pix(double[] transform, double geox, double geoy) {

        double x = (transform[0] * transform[5] -
                transform[2] * transform[3] + transform[2] * geoy -
                transform[5] * geox) / (transform[2] * transform[4] - transform[1] * transform[5]);

        double y = (transform[1] * transform[3] - transform[0] * transform[4] -
                transform[1] * geoy + transform[4] * geox) / (transform[2] * transform[4] - transform[1] * transform[5]);

        return new Vec2d(x, y);
    }

    /**
     * Convert pixel coordinates to map coordinates
     */
    public static Vec2d pix2wld(double[] transform, double x, double y) {
        double geox = transform[0] + transform[1] * x + transform[2] * y;
        double geoy = transform[3] + transform[4] * x + transform[5] * y;
        return new Vec2d(geox,geoy);
    }

}
