package space.celestia.mobilecelestia.utils;

import android.os.Build;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FontHelper {
    private static final String TAG = "FontHelper";
    private static final String systemFontPath = "/system/fonts/";

    private static final Map<FontCompat, FontCompat> fontReplacements = new HashMap<FontCompat, FontCompat>() {{}};

    @RequiresApi(Build.VERSION_CODES.Q)
    public static class Matcher {
        private long pointer;

        private Matcher(long ptr) {
            pointer = ptr;
        }

        public static @NonNull
        Matcher create() {
            return new Matcher(c_create());
        }

        public void setLocales(@NonNull String locales) {
            c_setLocales(pointer, locales);
        }

        public void setFamilyVariant(int familyVariant) {
            c_setFamilyVariant(pointer, familyVariant);
        }

        public void setStyle(int weight, boolean italic) {
            c_setStyle(pointer, weight, italic);
        }

        public @NonNull
        Font match(@NonNull String familyName, @NonNull String text) {
            return new Font(c_match(pointer, familyName, text));
        }

        public static Set<FontCompat> getAvailableFonts() {
            long iterPtr = c_openIterator();
            HashSet<FontCompat> nativeFonts = new HashSet<>();
            for (long fontPtr = c_getNext(iterPtr); fontPtr != 0; fontPtr = c_getNext(iterPtr)) {
                Font font = new Font(fontPtr);
                nativeFonts.add(new FontCompat(font.getFilePath(), font.getCollectionIndex()));
            }
            c_closeIterator(iterPtr);
            return nativeFonts;
        }

        @Override
        protected void finalize() throws Throwable {
            c_destroy(pointer);

            super.finalize();
        }

        private static native long c_create();

        private static native void c_destroy(long ptr);

        private static native void c_setLocales(long ptr, String locales);

        private static native void c_setFamilyVariant(long ptr, int familyVariant);

        private static native void c_setStyle(long ptr, int weight, boolean italic);

        private static native long c_match(long ptr, String familyName, String text);

        private static native long c_openIterator();

        private static native void c_closeIterator(long iterator);

        private static native long c_getNext(long iterator);

        static {
            System.loadLibrary("fonthelper");
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    public static class Font {
        private long pointer;

        private Font(long ptr) {
            pointer = ptr;
        }

        public @NonNull
        String getFilePath() {
            return c_getFontFilePath(pointer);
        }

        public int getCollectionIndex() {
            return c_getCollectionIndex(pointer);
        }

        @Override
        protected void finalize() throws Throwable {
            c_destroy(pointer);

            super.finalize();
        }

        private static native void c_destroy(long ptr);

        private static native String c_getFontFilePath(long ptr);

        private static native int c_getCollectionIndex(long ptr);
    }

    public static class FontCompat {
        final public @NonNull File file;
        final public int collectionIndex;
        final public @Nullable String name;

        public @NonNull String getFilePath() {
            return file.getPath();
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        private FontCompat(Font font) {
            file = new File(font.getFilePath());
            collectionIndex = font.getCollectionIndex();
            this.name = null;
        }

        public FontCompat(@NonNull String filePath, int collectionIndex, @Nullable String name) {
            this.file = new File(filePath);
            this.collectionIndex = collectionIndex;
            this.name = name;
        }

        public FontCompat(@NonNull String filePath, int collectionIndex) {
            this(filePath, collectionIndex, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FontCompat that = (FontCompat) o;
            return collectionIndex == that.collectionIndex &&
                    getFilePath().equals(that.getFilePath());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFilePath(), collectionIndex);
        }
    }

    public static @Nullable FontCompat getFontForLocale(@NonNull String locale, int weight) {
        FontCompat compat;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Finding font using SystemFont
            String probeText = "a";
            switch (locale) {
                case "zh_CN":
                case "zh_TW":
                case "ja":
                    probeText = "一";
                    break;
                case "ko":
                    probeText = "가";
                    break;
                case "ar":
                    probeText = "ئ";
                    break;
            }
            Matcher matcher = Matcher.create();
            matcher.setStyle(weight, false);
            compat = new FontCompat(matcher.match("sans-serif", probeText));
        }

        // Finding font according to the legacy fonts.xml
        String language = locale;
        if (language.equals("zh_CN"))
            language = "zh-Hans";
        else if (language.equals("zh_TW"))
            language = "zh-Hant";
        compat = LegacyFontConfig.getFontFallback(language, weight);

        if (compat == null)
            return compat;

        FontCompat replacement = fontReplacements.get(compat);
        if (replacement == null || !replacement.file.exists())
            return compat;
        return replacement;
    }

    static class LegacyFontConfig {
        static class FontFamily {
            final String language;
            final String name;
            private ArrayList<FontFace> faces;

            FontFamily(String name, String language) {
                this.name = name;
                this.language = language;
                faces = new ArrayList<>();
            }

            private void addFace(FontFace face) {
                faces.add(face);
            }
        }

        static class FontFace {
            final String fileName;
            final int collectionIndex;
            final int weight;
            final String style;

            FontFace(String fileName, int collectionIndex, String style, int weight) {
                this.fileName = fileName;
                this.collectionIndex = collectionIndex;
                this.weight = weight;
                this.style = style;
            }
        }

        private static boolean initialized = false;
        private static Map<String, List<FontFamily>> allFonts;

        private static final String fontXMLPath = "/system/etc/fonts.xml";

        private LegacyFontConfig() {}

        public static synchronized void init() {
            if (initialized) {
                return;
            }
            initialized = true;
            allFonts = new HashMap<>();
            parse();
        }

        private static void processDocument(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.nextTag();
            // Parse Families
            parser.require(XmlPullParser.START_TAG, null, "familyset");
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG || !"family".equals(parser.getName())) { continue; }

                // Parse family
                String name = parser.getAttributeValue(null, "name");
                String lang = parser.getAttributeValue(null, "lang");

                FontFamily family = new FontFamily(name, lang);

                // Getting faces for the family
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) { continue; }
                    try {
                        final String tag = parser.getName();
                        if ("font".equals(tag)) {
                            String weightStr = parser.getAttributeValue(null, "weight");
                            String indexStr = parser.getAttributeValue(null, "index");
                            String style = parser.getAttributeValue(null, "style");
                            if (weightStr == null) {
                                weightStr = "400";
                            }
                            if (indexStr == null) {
                                indexStr = "0";
                            }
                            int weight = Integer.parseInt(weightStr);
                            int index = Integer.parseInt(indexStr);
                            final String filename = parser.nextText();

                            FontFace face = new FontFace(systemFontPath + filename, index, style, weight);
                            if ((new File(face.fileName)).exists()) {
                                family.addFace(face);
                            }
                        } else {
                            skip(parser);
                        }
                    } catch (final XmlPullParserException e) {
                        Log.e(TAG, "Error in font.xml parsing: " + e.getMessage());
                    }
                }

                if (lang == null || lang.isEmpty()) {
                    addFamily("", family);
                } else {
                    // Languages are seperated by comma
                    String[] langs = lang.split(",");
                    for (String l : langs) {
                        addFamily(l, family);
                    }
                }
            }
        }

        private static void addFamily(String key, FontFamily family) {
            if (family.faces.size() == 0) { return; }
            if (allFonts.containsKey(key)) {
                Objects.requireNonNull(allFonts.get(key)).add(family);
            } else {
                ArrayList<FontFamily> list = new ArrayList<>();
                list.add(family);
                allFonts.put(key, list);
            }
        }

        private static void skip(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
            int depth = 1;
            while (depth > 0) {
                switch (parser.next()) {
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                }
            }
        }

        public static void parse() {
            File fontFile = new File(fontXMLPath);
            if (fontFile.exists()) {
                parse(fontFile);
            }
        }

        private static void parse(final File fileXml) {
            InputStream in;

            try {
                in = new FileInputStream(fileXml);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            final XmlPullParser parser = Xml.newPullParser();

            try {
                parser.setInput(in, null);
                processDocument(parser);
            } catch (final XmlPullParserException e) {
                Log.e(TAG, "Could not parse file: " + fileXml.getAbsolutePath() + " " + e.getMessage());

                e.printStackTrace();
            } catch (final IOException e) {
                Log.e(TAG, "Could not read file: " + fileXml.getAbsolutePath());
                e.printStackTrace();
            }

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static synchronized @Nullable
        FontCompat getFontFallback(String language, int expectedWeight) {
            if (!initialized)
                init();

            List<FontFamily> families = allFonts.get(language);
            if (families == null)
                families = allFonts.get(""); // Fallback to default

            if (families == null || families.size() == 0)
                return null;

            FontFamily family = families.get(0);

            FontFace closestFace = null;
            int difference = Integer.MAX_VALUE;

            for (int i = 0; i < family.faces.size(); i++) {
                FontFace face = family.faces.get(i);
                // We ignore italic ones
                if (face.style != null && face.style.equals("italic")) { continue; }

                // Finding the one that matches the best
                int newDifference = Math.abs(face.weight - expectedWeight);
                if (newDifference < difference) {
                    difference = newDifference;
                    closestFace = face;
                }
            }

            if (closestFace == null)
                return null;

            return new FontCompat(closestFace.fileName, closestFace.collectionIndex);
        }
    }
}