package space.celestia.mobilecelestia.core;

import java.io.Serializable;

public class CelestiaAstroObject implements Serializable {
    protected long pointer;

    protected CelestiaAstroObject(long ptr) {
        pointer = ptr;
    }
}
