package backend.utils;

public class Vec2d {

    public double x;
    public double y;

    public Vec2d() { }

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i round() {
        return new Vec2i((int)Math.round(x),(int)Math.round(y));
    }

    @Override
    public int hashCode() {
        return (23+HashUtils.hash(x)*31)*31+HashUtils.hash(y)*31;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Vec2d))return false;
        Vec2d o = (Vec2d)other;
        return x == o.x && y == o.y;
    }

    @Override
    protected Vec2d clone() {
        return new Vec2d(x,y);
    }

    @Override
    public String toString() {
        return "Vec2d( "+x+", "+y+" )";
    }

}
