package space.celestia.MobileCelestia.Core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CelestiaBrowserItem {
    public interface ChildrenProvider {
        Map<String, CelestiaBrowserItem> childrenForItem(CelestiaBrowserItem item);
    }

    private String _name;
    private CelestiaAstroObject _object;
    private ArrayList<String> childrenKeys;
    private ArrayList<CelestiaBrowserItem> childrenValues;
    private ChildrenProvider provider;

    public CelestiaBrowserItem(@Nullable String name, CelestiaAstroObject object, ChildrenProvider provider) {
        _name = name;
        _object = object;
        this.provider = provider;
    }

    public CelestiaBrowserItem(@Nullable String name, @NonNull Map<String, CelestiaBrowserItem> children) {
        _name = name;
        setChildren(children);
    }

    public @Nullable
    CelestiaAstroObject getObject() {
        return _object;
    }

    public @Nullable String getName() {
        return _name;
    }

    public @NonNull
    List<CelestiaBrowserItem> getChildren() {
        if (childrenValues == null && provider != null)
            setChildren(provider.childrenForItem(this));

        return childrenValues;
    }

    public String childNameAtIndex(int index) {
        if (childrenValues == null && provider != null)
            setChildren(provider.childrenForItem(this));

        return childrenKeys.get(index);
    }

    public @Nullable CelestiaBrowserItem childNamed(String name) {
        if (childrenValues == null && provider != null)
            setChildren(provider.childrenForItem(this));

        int index = childrenKeys.indexOf(name);
        if (index == -1) { return null; }
        return childrenValues.get(index);
    }

    private void setChildren(Map<String, CelestiaBrowserItem> children) {
        if (children == null) {
            childrenKeys = new ArrayList<String>();
            childrenValues = new ArrayList<CelestiaBrowserItem>();
            return;
        }
        childrenKeys = new ArrayList<>(children.keySet());
        // TODO: better sorting
        Collections.sort(childrenKeys);

        childrenValues = new ArrayList<>();
        for (int i = 0; i < childrenKeys.size(); i++) {
            childrenValues.add(children.get(childrenKeys.get(i)));
        }
    }
}
