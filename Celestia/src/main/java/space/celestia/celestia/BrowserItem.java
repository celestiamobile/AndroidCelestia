// BrowserItem.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BrowserItem {
    private final static String NAME_KEY = "name";
    private final static String ALTERNATIVE_NAME_KEY = "alternative_name";
    private final static String TYPE_KEY = "type";
    private final static int TYPE_BODY = 0;
    private final static int TYPE_LOCATION = 1;
    private final static String POINTER_KEY = "pointer";
    private final static String CHILDREN_KEY = "children";

    @RequiresApi(Build.VERSION_CODES.N)
    private static RuleBasedCollator collator = null;

    public interface ChildrenProvider {
        Map<String, BrowserItem> childrenForItem(BrowserItem item);
    }

    private String _name;
    private String _alternativeName;
    private AstroObject _object;
    private ArrayList<String> childrenKeys;
    private ArrayList<BrowserItem> childrenValues;
    private ChildrenProvider provider;

    public static class KeyValuePair {
        private final String key;
        private final BrowserItem value;

        public KeyValuePair(@NonNull String key, @NonNull BrowserItem value) {
            this.key = key;
            this.value = value;
        }
    }

    public BrowserItem(@NonNull String name, @Nullable String alternativeName, @NonNull AstroObject object, @Nullable ChildrenProvider provider) {
        _name = name;
        _alternativeName = alternativeName;
        _object = object;
        this.provider = provider;
    }

    public BrowserItem(@NonNull String name, @Nullable String alternativeName, @NonNull Map<String, BrowserItem> children) {
        _name = name;
        _alternativeName = alternativeName;
        setChildren(children);
    }

    public BrowserItem(@NonNull String name, @Nullable String alternativeName, @NonNull List<KeyValuePair> children) {
        _name = name;
        _alternativeName = alternativeName;
        setOrderedChildren(children);
    }

    private static BrowserItem fromJson(@NonNull JSONObject js, @Nullable ChildrenProvider provider) throws JSONException, RuntimeException {
        String name = js.getString(NAME_KEY);
        String alternativeName = null;
        if (js.has(ALTERNATIVE_NAME_KEY)) {
            alternativeName = js.getString(ALTERNATIVE_NAME_KEY);
        }
        if (js.has(POINTER_KEY) && js.has(TYPE_KEY)) {
            int type = js.getInt(TYPE_KEY);
            long ptr = js.getLong(POINTER_KEY);
            AstroObject object;
            if (type == TYPE_BODY) {
                object = new Body(ptr);
            } else if (type == TYPE_LOCATION) {
                object = new Location(ptr);
            } else {
                throw new RuntimeException(String.format("Unknown type found: %d.", type));
            }
            return new BrowserItem(name, alternativeName, object, provider);
        }
        HashMap<String, BrowserItem> items = new HashMap<>();
        if (js.has(CHILDREN_KEY)) {
            JSONObject children = js.getJSONObject(CHILDREN_KEY);
            Iterator<String> keys = children.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                items.put(key, fromJson(children.getJSONObject(key), provider));
            }
        }
        return new BrowserItem(name, alternativeName, items);
    }

    static Map<String, BrowserItem> fromJsonString(@NonNull String string, @Nullable ChildrenProvider provider) {
        HashMap<String, BrowserItem> items = new HashMap<>();
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
    AstroObject getObject() {
        return _object;
    }

    public @Nullable
    String getAlternativeName() {
        return _alternativeName;
    }

    public @NonNull String getName() {
        return _name;
    }

    public @NonNull
    List<BrowserItem> getChildren() {
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

    public @Nullable
    BrowserItem childNamed(String name) {
        if (childrenValues == null && provider != null)
            setChildren(provider.childrenForItem(this));

        int index = childrenKeys.indexOf(name);
        if (index == -1) { return null; }
        return childrenValues.get(index);
    }

    private void setOrderedChildren(List<KeyValuePair> children) {
        if (children == null) {
            childrenKeys = new ArrayList<>();
            childrenValues = new ArrayList<>();
            return;
        }

        childrenValues = new ArrayList<>();
        childrenKeys = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            KeyValuePair child = children.get(i);
            childrenKeys.add(child.key);
            childrenValues.add(child.value);
        }
    }

    private void setChildren(Map<String, BrowserItem> children) {
        if (children == null) {
            childrenKeys = new ArrayList<>();
            childrenValues = new ArrayList<>();
            return;
        }
        childrenKeys = new ArrayList<>(children.keySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (collator == null) {
                Collator newCollator = Collator.getInstance();
                if (newCollator instanceof RuleBasedCollator) {
                    collator = (RuleBasedCollator) newCollator;
                    collator.setNumericCollation(true);
                }
            }
            if (collator != null) {
                Collections.sort(childrenKeys, collator);
            } else {
                Collections.sort(childrenKeys);
            }
        } else {
            Collections.sort(childrenKeys);
        }

        childrenValues = new ArrayList<>();
        for (int i = 0; i < childrenKeys.size(); i++) {
            childrenValues.add(children.get(childrenKeys.get(i)));
        }
    }
}
