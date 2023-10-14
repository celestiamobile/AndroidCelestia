package space.celestia.celestia;

import androidx.annotation.NonNull;

public class Font {
    @NonNull public final String[] fontNames;
    @NonNull public final String path;
    public Font(@NonNull String path) {
        this.path = path;
        String[] names = c_getFontNames(path);
        if (names == null)
            fontNames = new String[] {};
        else
            fontNames = names;
    }

    private static native String[] c_getFontNames(String path);
}
