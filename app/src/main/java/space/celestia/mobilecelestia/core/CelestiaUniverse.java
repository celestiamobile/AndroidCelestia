package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

import java.util.Map;

public class CelestiaUniverse implements CelestiaBrowserItem.ChildrenProvider {
    protected long pointer;
    private CelestiaStarCatalog starCatalog;
    private CelestiaDSOCatalog dsoCatalog;

    CelestiaUniverse(long ptr) {
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
    public String getNameForSelection(CelestiaSelection selection) {
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

    @Override
    public Map<String, CelestiaBrowserItem> childrenForItem(CelestiaBrowserItem item) {
        CelestiaAstroObject obj = item.getObject();

        if (obj == null) { return null; }
        if (obj instanceof CelestiaStar)
            return c_getChildrenForStar(obj.pointer);
        if (obj instanceof CelestiaBody)
            return c_getChildrenForBody(obj.pointer);

        return null;
    }

    public @NonNull CelestiaSelection findObject(@NonNull String name) {
        return new CelestiaSelection(c_findObject(name));
    }

    // C functions
    private native long c_getStarCatalog();
    private native long c_getDSOCatalog();
    private native Map<String, CelestiaBrowserItem> c_getChildrenForStar(long pointer);
    private native Map<String, CelestiaBrowserItem> c_getChildrenForBody(long pointer);
    private native long c_findObject(String name);
}
