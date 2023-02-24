package space.celestia.celestia;

public class Galaxy extends DSO {
    protected Galaxy(long ptr) {
        super(ptr);
    }

    public float getRadius() {
        return c_getRadius(pointer);
    }

    public float getDetail() {
        return c_getDetail(pointer);
    }

    private native static float c_getRadius(long pointer);
    private native static float c_getDetail(long pointer);
}
