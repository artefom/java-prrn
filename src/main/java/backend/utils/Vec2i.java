package backend.utils;

public class Vec2i {
    public int x;
    public int y;

    public Vec2i () {};

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vec2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    @Override
    public int hashCode() {
        return (23+x*31)*31+y*31;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Vec2i))return false;
        Vec2i o = (Vec2i)other;
        return x == o.x && y == o.y;
    }

    @Override
    protected Vec2i clone() {
        return new Vec2i(x,y);
    }

    @Override
    public String toString() {
        return "Vec2i( "+x+", "+y+" )";
    }
}
