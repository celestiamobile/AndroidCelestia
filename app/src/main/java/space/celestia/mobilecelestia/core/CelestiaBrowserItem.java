package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CelestiaBrowserItem {
    private final static String NAME_KEY = "name";
    private final static String TYPE_KEY = "type";
    private final static int TYPE_BODY = 0;
    private final static int TYPE_LOCATION = 1;
    private final static String POINTER_KEY = "pointer";
    private final static String CHILDREN_KEY = "children";

    public interface ChildrenProvider {
        Map<String, CelestiaBrowserItem> childrenForItem(CelestiaBrowserItem item);
    }

    private String _name;
    private CelestiaAstroObject _object;
    private ArrayList<String> childrenKeys;
    private ArrayList<CelestiaBrowserItem> childrenValues;
    private ChildrenProvider provider;

    public CelestiaBrowserItem(@Nullable String name, @NonNull CelestiaAstroObject object, @Nullable ChildrenProvider provider) {
        _name = name;
        _object = object;
        this.provider = provider;
    }

    public CelestiaBrowserItem(@Nullable String name, @NonNull Map<String, CelestiaBrowserItem> children) {
        _name = name;
        setChildren(children);
    }

    private static CelestiaBrowserItem fromJson(@NonNull JSONObject js, @Nullable ChildrenProvider provider) throws JSONException, NullPointerException {
        String name = js.getString(NAME_KEY);
        if (js.has(POINTER_KEY) && js.has(TYPE_KEY)) {
            int type = js.getInt(TYPE_KEY);
            long ptr = js.getLong(POINTER_KEY);
            CelestiaAstroObject object = null;
            if (type == TYPE_BODY) {
                object = new CelestiaBody(ptr);
            } else if (type == TYPE_LOCATION) {
                object = new CelestiaLocation(ptr);
            } else {
                // unknown type
            }
            return new CelestiaBrowserItem(name, object, provider);
        }
        HashMap<String, CelestiaBrowserItem> items = new HashMap<>();
        if (js.has(CHILDREN_KEY)) {
            JSONObject children = js.getJSONObject(CHILDREN_KEY);
            Iterator<String> keys = children.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                items.put(key, fromJson(children.getJSONObject(key), provider));
            }
        }
        return new CelestiaBrowserItem(name, items);
    }

    static Map<String, CelestiaBrowserItem> fromJsonString(@NonNull String string, @Nullable ChildrenProvider provider) {
        HashMap<String, CelestiaBrowserItem> items = new HashMap<>();
        try {
            JSONObject js = new JSONObject(string);
            Iterator<String> keys = js.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                items.put(key, fromJson(js.getJSONObject(key), provider));
            }
        } catch (Exception ignored) {}
        return items;
    }

    public @Nullable
    CelestiaAstroObject getObject() {
        return _object;
    }

    public @NonNull String getName() {
        if (_name == null)
            return "";
        return _name;
    }

    public @NonNull
    List<CelestiaBrowserItem> getChildren() {
        if (childrenValues == null && provider != null)
            setChildren(provider.childrenForItem(this));

        if (childrenValues == null)
            return new ArrayList<>();

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
