package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;

public class CelestiaUniverse {
    protected long pointer;
    private CelestiaStarCatalog starCatalog;
    private CelestiaDSOCatalog dsoCatalog;

    protected CelestiaUniverse(long ptr) {
        pointer = ptr;
    }

    @NonNull
    public CelestiaStarCatalog getStarCatalog() {
        if (starCatalog == null)
            starCatalog = new CelestiaStarCatalog(c_getStarCatalog());
        return starCatalog;
    }

    @NonNull
    public CelestiaDSOCatalog getDSOCatalog() {
        if (dsoCatalog == null)
            dsoCatalog = new CelestiaDSOCatalog(c_getDSOCatalog());
        return dsoCatalog;
    }

    @NonNull
    public String nameForSelection(CelestiaSelection selection) {
        CelestiaStar star = selection.getStar();
        if (star != null)
            return getStarCatalog().getStarName(star);
        CelestiaDSO dso = selection.getDSO();
        if (dso != null)
            return getDSOCatalog().getDSOName(dso);
        CelestiaBody body = selection.getBody();
        if (body != null)
            return body.getName();
        CelestiaLocation location = selection.getLocation();
        if (location != null)
            return location.getName();
        return "";
    }

    // C functions
    private native long c_getStarCatalog();
    private native long c_getDSOCatalog();
}
