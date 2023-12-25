/*
 * AppCore.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppCore {
    public static final int MOUSE_BUTTON_LEFT       = 0x01;
    public static final int MOUSE_BUTTON_MIDDLE     = 0x02;
    public static final int MOUSE_BUTTON_RIGHT      = 0x04;
    public static final int SHIFT_KEY               = 0x08;
    public static final int CONTROL_KEY             = 0x10;

    public static final int JOYSTICK_AXIS_X         = 0;
    public static final int JOYSTICK_AXIS_Y         = 1;
    public static final int JOYSTICK_AXIS_Z         = 2;

    public static final int MEASUREMENT_SYSTEM_METRIC   = 0;
    public static final int MEASUREMENT_SYSTEM_IMPERIAL = 1;

    public static final int LAYOUT_DIRECTION_LTR   = 0;
    public static final int LAYOUT_DIRECTION_RTL   = 1;

    public static final int RENDER_FONT_STYLE_NORMAL    = 0;
    public static final int RENDER_FONT_STYLE_LARGE     = 1;

    public static final int IMAGE_TYPE_JPEG     = 1;
    public static final int IMAGE_TYPE_PNG      = 4;

    public interface ProgressWatcher {
        void onCelestiaProgress(@NonNull String progress);
    }

    public interface ContextMenuHandler {
        void requestContextMenu(float x, float y, @NonNull Selection selection);
    }

    public interface FatalErrorHandler {
        void fatalError(@NonNull String message);
    }
    private final long pointer;
    private boolean initialized;
    private Simulation simulation;
    private ContextMenuHandler contextMenuHandler;
    private FatalErrorHandler fatalErrorHandler;

    public boolean isInitialized() {
        return initialized;
    }

    public AppCore() {
        this.pointer = c_init();
        this.initialized = false;
        this.simulation = null;
        this.contextMenuHandler = null;
        c_setContextMenuHandler(pointer);
        c_setFatalErrorHandler(pointer);
    }

    public boolean startRenderer() {
        return c_startRenderer(pointer);
    }

    public boolean startSimulation(@Nullable String configFileName, @Nullable String[] extraDirectories, @Nullable ProgressWatcher watcher) {
        initialized = c_startSimulation(pointer, configFileName, extraDirectories, watcher);
        return initialized;
    }

    public void start() {
        c_start(pointer);
    }

    public void start(@NonNull Date date) {
        c_start(pointer, (double)date.getTime() / 1000);
    }

    public void draw() {
        c_draw(pointer);
    }

    public void tick() {
        c_tick(pointer);
    }

    public void resize(int w, int h) {
        c_resize(pointer, w, h);
    }

    public int getWidth() {
        return c_getWidth(pointer);
    }

    public int getHeight() {
        return c_getHeight(pointer);
    }

    public void setContextMenuHandler(@Nullable ContextMenuHandler handler) {
        contextMenuHandler = handler;
    }

    public void setFatalErrorHandler(@Nullable FatalErrorHandler handler) {
        fatalErrorHandler = handler;
    }

    private void onRequestContextMenu(float x, float y, Selection selection) {
        if (contextMenuHandler != null)
            contextMenuHandler.requestContextMenu(x, y, selection);
    }

    private void onFatalError(String message) {
        if (fatalErrorHandler != null)
            fatalErrorHandler.fatalError(message);
    }

    public void setSafeAreaInsets(int left, int top, int right, int bottom) {
        c_setSafeAreaInsets(pointer, left, top, right, bottom);
    }

    public void setDPI(int dpi) {
        c_setDPI(pointer, dpi);
    }

    // Control
    public void mouseButtonUp(int buttons, @NonNull PointF point, int modifiers) {
        c_mouseButtonUp(pointer, buttons, point.x, point.y, modifiers);
    }

    public void mouseButtonDown(int buttons, @NonNull PointF point, int modifiers) {
        c_mouseButtonDown(pointer, buttons, point.x, point.y, modifiers);
    }

    public void mouseMove(int buttons, @NonNull PointF offset, int modifiers) {
        c_mouseMove(pointer, buttons, offset.x, offset.y, modifiers);
    }

    public void mouseWheel(float motion, int modifiers) {
        c_mouseWheel(pointer, motion, modifiers);
    }

    public void keyUp(int input) {
        c_keyUp(pointer, input);
    }

    public void keyDown(int input) {
        c_keyDown(pointer, input);
    }

    public void keyUp(int input, int key, int modifiers) {
        c_keyUpWithModifiers(pointer, input, key, modifiers);
    }

    public void keyDown(int input, int key, int modifers) {
        c_keyDownWithModifers(pointer, input, key, modifers);
    }

    public void charEnter(int input) {
        c_charEnter(pointer, input);
    }

    public void joystickButtonDown(int button) {
        c_joystickButtonDown(pointer, button);
    }

    public void joystickButtonUp(int button) {
        c_joystickButtonUp(pointer, button);
    }

    public void joystickAxis(int axis, float amount) {
        c_joystickAxis(pointer, axis, amount);
    }

    public void pinchUpdate(@NonNull PointF focus, float scale) {
        c_pinchUpdate(pointer, focus.x, focus.y, scale);
    }

    public void runScript(@NonNull String scriptPath) { c_runScript(pointer, scriptPath); }
    public void runDemo() { c_runDemo(pointer); }
    public @NonNull String getCurrentURL() { return c_getCurrentURL(pointer); }
    public void goToURL(@NonNull String url) { c_goToURL(pointer, url); }

    public void setFont(@NonNull String fontPath, int collectionIndex, int fontSize) {
        c_setFont(pointer, fontPath, collectionIndex, fontSize);
    }

    public void setTitleFont(@NonNull String fontPath, int collectionIndex, int fontSize) {
        c_setTitleFont(pointer, fontPath, collectionIndex, fontSize);
    }

    public void setRendererFont(@NonNull String fontPath, int collectionIndex, int fontSize, int fontStyle) {
        c_setRendererFont(pointer, fontPath, collectionIndex, fontSize, fontStyle);
    }

    public void setPickTolerance(float pickTolerance) {
        c_setPickTolerance(pointer, pickTolerance);
    }

    public static boolean initGL() {
        return c_initGL();
    }
    public static void chdir(String path) { c_chdir(path);}

    public void setRenderer(Renderer renderer) {
        renderer.setCorePointer(pointer);
    }
    public boolean saveScreenshot(@NonNull String filePath, int imageType) {
        return c_saveScreenshot(pointer, filePath, imageType);
    }

    // Locale
    public static @NonNull String getLanguage() { return c_getLanguage(); };
    public static void setLocaleDirectoryPath(@NonNull String localeDirectoryPath, @NonNull String locale) { c_setLocaleDirectoryPath(localeDirectoryPath, locale); }
    public static void setUpLocale() { c_setUpLocale(); }
    public static @NonNull String getLocalizedString(@NonNull String string) { return getLocalizedString(string, "celestia_ui"); }
    public static @NonNull String getLocalizedString(@NonNull String string, @NonNull String domain) { return c_getLocalizedString(string, domain); }
    public static @NonNull String getLocalizedFilename(@NonNull String filename) { return c_getLocalizedFilename(filename); }
    public Simulation getSimulation() {
        if (simulation == null)
            simulation = new Simulation(c_getSimulation(pointer));
        return simulation;
    }

    public @NonNull String getRenderInfo() {
        return c_getRenderInfo(pointer);
    }

    public @NonNull List<Destination> getDestinations() {
        return c_getDestinations(pointer);
    }

    // C function
    private native void c_setContextMenuHandler(long ptr);
    private native void c_setFatalErrorHandler(long ptr);
    private static native long c_init();
    private static native boolean c_startRenderer(long ptr);
    private static native boolean c_startSimulation(long ptr, String configFileName, String[] extraDirectories, ProgressWatcher watcher);
    private static native void c_start(long ptr);
    private static native void c_start(long ptr, double secondsSinceEpoch);
    private static native void c_draw(long ptr);
    private static native void c_tick(long ptr);
    private static native void c_resize(long ptr, int w, int h);
    private static native int c_getWidth(long ptr);
    private static native int c_getHeight(long ptr);
    private static native void c_setSafeAreaInsets(long ptr, int left, int top, int right, int bottom);
    private static native void c_setDPI(long ptr, int dpi);
    private static native long c_getSimulation(long ptr);

    // Control
    private static native void c_mouseButtonUp(long ptr, int buttons, float x, float y, int modifiers);
    private static native void c_mouseButtonDown(long ptr, int buttons, float x, float y, int modifiers);
    private static native void c_mouseMove(long ptr, int buttons, float x, float y, int modifiers);
    private static native void c_mouseWheel(long ptr, float motion, int modifiers);
    private static native void c_keyUpWithModifiers(long ptr, int input, int key, int modifiers);
    private static native void c_keyDownWithModifers(long ptr, int input, int key, int modifiers);
    private static native void c_keyUp(long ptr, int input);
    private static native void c_keyDown(long ptr, int input);
    private static native void c_charEnter(long ptr, int input);
    private static native void c_joystickButtonDown(long ptr, int button);
    private static native void c_joystickButtonUp(long ptr, int button);
    private static native void c_joystickAxis(long ptr, int axis, float amount);
    private static native void c_pinchUpdate(long ptr, float focusX, float focusY, float scale);
    private static native void c_runScript(long ptr, String path);
    private static native void c_runDemo(long ptr);
    private static native String c_getCurrentURL(long ptr);
    private static native void c_goToURL(long ptr, String url);

    private static native void c_setFont(long ptr, String fontPath, int collectionIndex, int fontSize);
    private static native void c_setTitleFont(long ptr, String fontPath, int collectionIndex, int fontSize);
    private static native void c_setRendererFont(long ptr, String fontPath, int collectionIndex, int fontSize, int fontStyle);

    private static native void c_setPickTolerance(long ptr, float pickTolerance);

    private static native boolean c_initGL();
    private static native void c_chdir(String path);

    private static native List<Destination> c_getDestinations(long ptr);

    // Locale
    private static native String c_getLanguage();
    private static native void c_setLocaleDirectoryPath(String path, String locale);
    private static native void c_setUpLocale();
    private static native String c_getLocalizedString(String string, String domain);
    private static native String c_getLocalizedFilename(String filename);

    private static native String c_getRenderInfo(long ptr);

    private static native boolean c_saveScreenshot(long ptr, String filePath, int imageType);

    private final static String TAG = "CelestiaAppCore";

    // Settings
    public boolean getBooleanValueForPield(@NonNull String field) {
        try {
            Method method = getClass().getDeclaredMethod("get" + field);
            Object value = method.invoke(this);
            return (boolean)value;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get field " + field);
        }
        return false;
    }

    public void setBooleanValueForField(@NonNull String field, boolean value) {
        try {
            Method method = getClass().getDeclaredMethod("set" + field, boolean.class);
            method.invoke(this, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set field " + field);
        }
    }

    public int getIntValueForField(@NonNull String field) {
        try {
            Method method = getClass().getDeclaredMethod("get" + field);
            Object value = method.invoke(this);
            return (int)value;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get field " + field);
        }
        return 0;
    }

    public void setIntValueForField(@NonNull String field, int value) {
        try {
            Method method = getClass().getDeclaredMethod("set" + field, int.class);
            method.invoke(this, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set field " + field);
        }
    }

    public void setDoubleValueForField(@NonNull String field, double value) {
        try {
            Method method = getClass().getDeclaredMethod("set" + field, double.class);
            method.invoke(this, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set field " + field);
        }
    }

    public double getDoubleValueForField(@NonNull String field) {
        try {
            Method method = getClass().getDeclaredMethod("get" + field);
            Object value = method.invoke(this);
            return (double)value;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get field " + field);
        }
        return 0.0;
    }

    public boolean getShowStars() { return c_getShowStars(pointer); }
    public void setShowStars(boolean showStars) { c_setShowStars(pointer, showStars); }
    private static native void c_setShowStars(long pointer, boolean showStars);
    private native boolean c_getShowStars(long pointer);
    public boolean getShowPlanets() { return c_getShowPlanets(pointer); }
    public void setShowPlanets(boolean showPlanets) { c_setShowPlanets(pointer, showPlanets); }
    private static native void c_setShowPlanets(long pointer, boolean showPlanets);
    private native boolean c_getShowPlanets(long pointer);
    public boolean getShowDwarfPlanets() { return c_getShowDwarfPlanets(pointer); }
    public void setShowDwarfPlanets(boolean showDwarfPlanets) { c_setShowDwarfPlanets(pointer, showDwarfPlanets); }
    private static native void c_setShowDwarfPlanets(long pointer, boolean showDwarfPlanets);
    private native boolean c_getShowDwarfPlanets(long pointer);
    public boolean getShowMoons() { return c_getShowMoons(pointer); }
    public void setShowMoons(boolean showMoons) { c_setShowMoons(pointer, showMoons); }
    private static native void c_setShowMoons(long pointer, boolean showMoons);
    private native boolean c_getShowMoons(long pointer);
    public boolean getShowMinorMoons() { return c_getShowMinorMoons(pointer); }
    public void setShowMinorMoons(boolean showMinorMoons) { c_setShowMinorMoons(pointer, showMinorMoons); }
    private static native void c_setShowMinorMoons(long pointer, boolean showMinorMoons);
    private native boolean c_getShowMinorMoons(long pointer);
    public boolean getShowAsteroids() { return c_getShowAsteroids(pointer); }
    public void setShowAsteroids(boolean showAsteroids) { c_setShowAsteroids(pointer, showAsteroids); }
    private static native void c_setShowAsteroids(long pointer, boolean showAsteroids);
    private native boolean c_getShowAsteroids(long pointer);
    public boolean getShowComets() { return c_getShowComets(pointer); }
    public void setShowComets(boolean showComets) { c_setShowComets(pointer, showComets); }
    private static native void c_setShowComets(long pointer, boolean showComets);
    private native boolean c_getShowComets(long pointer);
    public boolean getShowSpacecrafts() { return c_getShowSpacecrafts(pointer); }
    public void setShowSpacecrafts(boolean showSpacecrafts) { c_setShowSpacecrafts(pointer, showSpacecrafts); }
    private static native void c_setShowSpacecrafts(long pointer, boolean showSpacecrafts);
    private native boolean c_getShowSpacecrafts(long pointer);
    public boolean getShowGalaxies() { return c_getShowGalaxies(pointer); }
    public void setShowGalaxies(boolean showGalaxies) { c_setShowGalaxies(pointer, showGalaxies); }
    private static native void c_setShowGalaxies(long pointer, boolean showGalaxies);
    private native boolean c_getShowGalaxies(long pointer);
    public boolean getShowGlobulars() { return c_getShowGlobulars(pointer); }
    public void setShowGlobulars(boolean showGlobulars) { c_setShowGlobulars(pointer, showGlobulars); }
    private static native void c_setShowGlobulars(long pointer, boolean showGlobulars);
    private native boolean c_getShowGlobulars(long pointer);
    public boolean getShowNebulae() { return c_getShowNebulae(pointer); }
    public void setShowNebulae(boolean showNebulae) { c_setShowNebulae(pointer, showNebulae); }
    private static native void c_setShowNebulae(long pointer, boolean showNebulae);
    private native boolean c_getShowNebulae(long pointer);
    public boolean getShowOpenClusters() { return c_getShowOpenClusters(pointer); }
    public void setShowOpenClusters(boolean showOpenClusters) { c_setShowOpenClusters(pointer, showOpenClusters); }
    private static native void c_setShowOpenClusters(long pointer, boolean showOpenClusters);
    private native boolean c_getShowOpenClusters(long pointer);
    public boolean getShowDiagrams() { return c_getShowDiagrams(pointer); }
    public void setShowDiagrams(boolean showDiagrams) { c_setShowDiagrams(pointer, showDiagrams); }
    private static native void c_setShowDiagrams(long pointer, boolean showDiagrams);
    private native boolean c_getShowDiagrams(long pointer);
    public boolean getShowBoundaries() { return c_getShowBoundaries(pointer); }
    public void setShowBoundaries(boolean showBoundaries) { c_setShowBoundaries(pointer, showBoundaries); }
    private static native void c_setShowBoundaries(long pointer, boolean showBoundaries);
    private native boolean c_getShowBoundaries(long pointer);
    public boolean getShowCloudMaps() { return c_getShowCloudMaps(pointer); }
    public void setShowCloudMaps(boolean showCloudMaps) { c_setShowCloudMaps(pointer, showCloudMaps); }
    private static native void c_setShowCloudMaps(long pointer, boolean showCloudMaps);
    private native boolean c_getShowCloudMaps(long pointer);
    public boolean getShowNightMaps() { return c_getShowNightMaps(pointer); }
    public void setShowNightMaps(boolean showNightMaps) { c_setShowNightMaps(pointer, showNightMaps); }
    private static native void c_setShowNightMaps(long pointer, boolean showNightMaps);
    private native boolean c_getShowNightMaps(long pointer);
    public boolean getShowAtmospheres() { return c_getShowAtmospheres(pointer); }
    public void setShowAtmospheres(boolean showAtmospheres) { c_setShowAtmospheres(pointer, showAtmospheres); }
    private static native void c_setShowAtmospheres(long pointer, boolean showAtmospheres);
    private native boolean c_getShowAtmospheres(long pointer);
    public boolean getShowCometTails() { return c_getShowCometTails(pointer); }
    public void setShowCometTails(boolean showCometTails) { c_setShowCometTails(pointer, showCometTails); }
    private static native void c_setShowCometTails(long pointer, boolean showCometTails);
    private native boolean c_getShowCometTails(long pointer);
    public boolean getShowPlanetRings() { return c_getShowPlanetRings(pointer); }
    public void setShowPlanetRings(boolean showPlanetRings) { c_setShowPlanetRings(pointer, showPlanetRings); }
    private static native void c_setShowPlanetRings(long pointer, boolean showPlanetRings);
    private native boolean c_getShowPlanetRings(long pointer);
    public boolean getShowMarkers() { return c_getShowMarkers(pointer); }
    public void setShowMarkers(boolean showMarkers) { c_setShowMarkers(pointer, showMarkers); }
    private static native void c_setShowMarkers(long pointer, boolean showMarkers);
    private native boolean c_getShowMarkers(long pointer);
    public boolean getShowOrbits() { return c_getShowOrbits(pointer); }
    public void setShowOrbits(boolean showOrbits) { c_setShowOrbits(pointer, showOrbits); }
    private static native void c_setShowOrbits(long pointer, boolean showOrbits);
    private native boolean c_getShowOrbits(long pointer);
    public boolean getShowFadingOrbits() { return c_getShowFadingOrbits(pointer); }
    public void setShowFadingOrbits(boolean showFadingOrbits) { c_setShowFadingOrbits(pointer, showFadingOrbits); }
    private static native void c_setShowFadingOrbits(long pointer, boolean showFadingOrbits);
    private native boolean c_getShowFadingOrbits(long pointer);
    public boolean getShowPartialTrajectories() { return c_getShowPartialTrajectories(pointer); }
    public void setShowPartialTrajectories(boolean showPartialTrajectories) { c_setShowPartialTrajectories(pointer, showPartialTrajectories); }
    private static native void c_setShowPartialTrajectories(long pointer, boolean showPartialTrajectories);
    private native boolean c_getShowPartialTrajectories(long pointer);
    public boolean getShowSmoothLines() { return c_getShowSmoothLines(pointer); }
    public void setShowSmoothLines(boolean showSmoothLines) { c_setShowSmoothLines(pointer, showSmoothLines); }
    private static native void c_setShowSmoothLines(long pointer, boolean showSmoothLines);
    private native boolean c_getShowSmoothLines(long pointer);
    public boolean getShowEclipseShadows() { return c_getShowEclipseShadows(pointer); }
    public void setShowEclipseShadows(boolean showEclipseShadows) { c_setShowEclipseShadows(pointer, showEclipseShadows); }
    private static native void c_setShowEclipseShadows(long pointer, boolean showEclipseShadows);
    private native boolean c_getShowEclipseShadows(long pointer);
    public boolean getShowRingShadows() { return c_getShowRingShadows(pointer); }
    public void setShowRingShadows(boolean showRingShadows) { c_setShowRingShadows(pointer, showRingShadows); }
    private static native void c_setShowRingShadows(long pointer, boolean showRingShadows);
    private native boolean c_getShowRingShadows(long pointer);
    public boolean getShowCloudShadows() { return c_getShowCloudShadows(pointer); }
    public void setShowCloudShadows(boolean showCloudShadows) { c_setShowCloudShadows(pointer, showCloudShadows); }
    private static native void c_setShowCloudShadows(long pointer, boolean showCloudShadows);
    private native boolean c_getShowCloudShadows(long pointer);
    public boolean getShowAutoMag() { return c_getShowAutoMag(pointer); }
    public void setShowAutoMag(boolean showAutoMag) { c_setShowAutoMag(pointer, showAutoMag); }
    private static native void c_setShowAutoMag(long pointer, boolean showAutoMag);
    private native boolean c_getShowAutoMag(long pointer);
    public boolean getShowCelestialSphere() { return c_getShowCelestialSphere(pointer); }
    public void setShowCelestialSphere(boolean showCelestialSphere) { c_setShowCelestialSphere(pointer, showCelestialSphere); }
    private static native void c_setShowCelestialSphere(long pointer, boolean showCelestialSphere);
    private native boolean c_getShowCelestialSphere(long pointer);
    public boolean getShowEclipticGrid() { return c_getShowEclipticGrid(pointer); }
    public void setShowEclipticGrid(boolean showEclipticGrid) { c_setShowEclipticGrid(pointer, showEclipticGrid); }
    private static native void c_setShowEclipticGrid(long pointer, boolean showEclipticGrid);
    private native boolean c_getShowEclipticGrid(long pointer);
    public boolean getShowHorizonGrid() { return c_getShowHorizonGrid(pointer); }
    public void setShowHorizonGrid(boolean showHorizonGrid) { c_setShowHorizonGrid(pointer, showHorizonGrid); }
    private static native void c_setShowHorizonGrid(long pointer, boolean showHorizonGrid);
    private native boolean c_getShowHorizonGrid(long pointer);
    public boolean getShowGalacticGrid() { return c_getShowGalacticGrid(pointer); }
    public void setShowGalacticGrid(boolean showGalacticGrid) { c_setShowGalacticGrid(pointer, showGalacticGrid); }
    private static native void c_setShowGalacticGrid(long pointer, boolean showGalacticGrid);
    private native boolean c_getShowGalacticGrid(long pointer);
    public boolean getShowEcliptic() { return c_getShowEcliptic(pointer); }
    public void setShowEcliptic(boolean showEcliptic) { c_setShowEcliptic(pointer, showEcliptic); }
    private static native void c_setShowEcliptic(long pointer, boolean showEcliptic);
    private native boolean c_getShowEcliptic(long pointer);

    public boolean getShowStarLabels() { return c_getShowStarLabels(pointer); }
    public void setShowStarLabels(boolean showStarLabels) { c_setShowStarLabels(pointer, showStarLabels); }
    private static native void c_setShowStarLabels(long pointer, boolean showStarLabels);
    private native boolean c_getShowStarLabels(long pointer);
    public boolean getShowPlanetLabels() { return c_getShowPlanetLabels(pointer); }
    public void setShowPlanetLabels(boolean showPlanetLabels) { c_setShowPlanetLabels(pointer, showPlanetLabels); }
    private static native void c_setShowPlanetLabels(long pointer, boolean showPlanetLabels);
    private native boolean c_getShowPlanetLabels(long pointer);
    public boolean getShowMoonLabels() { return c_getShowMoonLabels(pointer); }
    public void setShowMoonLabels(boolean showMoonLabels) { c_setShowMoonLabels(pointer, showMoonLabels); }
    private static native void c_setShowMoonLabels(long pointer, boolean showMoonLabels);
    private native boolean c_getShowMoonLabels(long pointer);
    public boolean getShowConstellationLabels() { return c_getShowConstellationLabels(pointer); }
    public void setShowConstellationLabels(boolean showConstellationLabels) { c_setShowConstellationLabels(pointer, showConstellationLabels); }
    private static native void c_setShowConstellationLabels(long pointer, boolean showConstellationLabels);
    private native boolean c_getShowConstellationLabels(long pointer);
    public boolean getShowGalaxyLabels() { return c_getShowGalaxyLabels(pointer); }
    public void setShowGalaxyLabels(boolean showGalaxyLabels) { c_setShowGalaxyLabels(pointer, showGalaxyLabels); }
    private static native void c_setShowGalaxyLabels(long pointer, boolean showGalaxyLabels);
    private native boolean c_getShowGalaxyLabels(long pointer);
    public boolean getShowGlobularLabels() { return c_getShowGlobularLabels(pointer); }
    public void setShowGlobularLabels(boolean showGlobularLabels) { c_setShowGlobularLabels(pointer, showGlobularLabels); }
    private static native void c_setShowGlobularLabels(long pointer, boolean showGlobularLabels);
    private native boolean c_getShowGlobularLabels(long pointer);
    public boolean getShowNebulaLabels() { return c_getShowNebulaLabels(pointer); }
    public void setShowNebulaLabels(boolean showNebulaLabels) { c_setShowNebulaLabels(pointer, showNebulaLabels); }
    private static native void c_setShowNebulaLabels(long pointer, boolean showNebulaLabels);
    private native boolean c_getShowNebulaLabels(long pointer);
    public boolean getShowOpenClusterLabels() { return c_getShowOpenClusterLabels(pointer); }
    public void setShowOpenClusterLabels(boolean showOpenClusterLabels) { c_setShowOpenClusterLabels(pointer, showOpenClusterLabels); }
    private static native void c_setShowOpenClusterLabels(long pointer, boolean showOpenClusterLabels);
    private native boolean c_getShowOpenClusterLabels(long pointer);
    public boolean getShowAsteroidLabels() { return c_getShowAsteroidLabels(pointer); }
    public void setShowAsteroidLabels(boolean showAsteroidLabels) { c_setShowAsteroidLabels(pointer, showAsteroidLabels); }
    private static native void c_setShowAsteroidLabels(long pointer, boolean showAsteroidLabels);
    private native boolean c_getShowAsteroidLabels(long pointer);
    public boolean getShowSpacecraftLabels() { return c_getShowSpacecraftLabels(pointer); }
    public void setShowSpacecraftLabels(boolean showSpacecraftLabels) { c_setShowSpacecraftLabels(pointer, showSpacecraftLabels); }
    private static native void c_setShowSpacecraftLabels(long pointer, boolean showSpacecraftLabels);
    private native boolean c_getShowSpacecraftLabels(long pointer);
    public boolean getShowLocationLabels() { return c_getShowLocationLabels(pointer); }
    public void setShowLocationLabels(boolean showLocationLabels) { c_setShowLocationLabels(pointer, showLocationLabels); }
    private static native void c_setShowLocationLabels(long pointer, boolean showLocationLabels);
    private native boolean c_getShowLocationLabels(long pointer);
    public boolean getShowCometLabels() { return c_getShowCometLabels(pointer); }
    public void setShowCometLabels(boolean showCometLabels) { c_setShowCometLabels(pointer, showCometLabels); }
    private static native void c_setShowCometLabels(long pointer, boolean showCometLabels);
    private native boolean c_getShowCometLabels(long pointer);
    public boolean getShowDwarfPlanetLabels() { return c_getShowDwarfPlanetLabels(pointer); }
    public void setShowDwarfPlanetLabels(boolean showDwarfPlanetLabels) { c_setShowDwarfPlanetLabels(pointer, showDwarfPlanetLabels); }
    private static native void c_setShowDwarfPlanetLabels(long pointer, boolean showDwarfPlanetLabels);
    private native boolean c_getShowDwarfPlanetLabels(long pointer);
    public boolean getShowMinorMoonLabels() { return c_getShowMinorMoonLabels(pointer); }
    public void setShowMinorMoonLabels(boolean showMinorMoonLabels) { c_setShowMinorMoonLabels(pointer, showMinorMoonLabels); }
    private static native void c_setShowMinorMoonLabels(long pointer, boolean showMinorMoonLabels);
    private native boolean c_getShowMinorMoonLabels(long pointer);

    // ShowLatinConstellationLabels (UI) is the opposite value of ShowI18nConstellationLabels
    public boolean getShowLatinConstellationLabels() { return !c_getShowI18nConstellationLabels(pointer); }
    public void setShowLatinConstellationLabels(boolean showLatinConstellationLabels) { c_setShowI18nConstellationLabels(pointer, !showLatinConstellationLabels); }
    private static native void c_setShowI18nConstellationLabels(long pointer, boolean showI18nConstellationLabels);
    private static native boolean c_getShowI18nConstellationLabels(long pointer);

    public boolean getShowPlanetOrbits() { return c_getShowPlanetOrbits(pointer); }
    public void setShowPlanetOrbits(boolean showPlanetOrbits) { c_setShowPlanetOrbits(pointer, showPlanetOrbits); }
    private static native void c_setShowPlanetOrbits(long pointer, boolean showPlanetOrbits);
    private native boolean c_getShowPlanetOrbits(long pointer);
    public boolean getShowMoonOrbits() { return c_getShowMoonOrbits(pointer); }
    public void setShowMoonOrbits(boolean showMoonOrbits) { c_setShowMoonOrbits(pointer, showMoonOrbits); }
    private static native void c_setShowMoonOrbits(long pointer, boolean showMoonOrbits);
    private native boolean c_getShowMoonOrbits(long pointer);
    public boolean getShowAsteroidOrbits() { return c_getShowAsteroidOrbits(pointer); }
    public void setShowAsteroidOrbits(boolean showAsteroidOrbits) { c_setShowAsteroidOrbits(pointer, showAsteroidOrbits); }
    private static native void c_setShowAsteroidOrbits(long pointer, boolean showAsteroidOrbits);
    private native boolean c_getShowAsteroidOrbits(long pointer);
    public boolean getShowSpacecraftOrbits() { return c_getShowSpacecraftOrbits(pointer); }
    public void setShowSpacecraftOrbits(boolean showSpacecraftOrbits) { c_setShowSpacecraftOrbits(pointer, showSpacecraftOrbits); }
    private static native void c_setShowSpacecraftOrbits(long pointer, boolean showSpacecraftOrbits);
    private native boolean c_getShowSpacecraftOrbits(long pointer);
    public boolean getShowCometOrbits() { return c_getShowCometOrbits(pointer); }
    public void setShowCometOrbits(boolean showCometOrbits) { c_setShowCometOrbits(pointer, showCometOrbits); }
    private static native void c_setShowCometOrbits(long pointer, boolean showCometOrbits);
    private native boolean c_getShowCometOrbits(long pointer);
    public boolean getShowStellarOrbits() { return c_getShowStellarOrbits(pointer); }
    public void setShowStellarOrbits(boolean showStellarOrbits) { c_setShowStellarOrbits(pointer, showStellarOrbits); }
    private static native void c_setShowStellarOrbits(long pointer, boolean showStellarOrbits);
    private native boolean c_getShowStellarOrbits(long pointer);
    public boolean getShowDwarfPlanetOrbits() { return c_getShowDwarfPlanetOrbits(pointer); }
    public void setShowDwarfPlanetOrbits(boolean showDwarfPlanetOrbits) { c_setShowDwarfPlanetOrbits(pointer, showDwarfPlanetOrbits); }
    private static native void c_setShowDwarfPlanetOrbits(long pointer, boolean showDwarfPlanetOrbits);
    private native boolean c_getShowDwarfPlanetOrbits(long pointer);
    public boolean getShowMinorMoonOrbits() { return c_getShowMinorMoonOrbits(pointer); }
    public void setShowMinorMoonOrbits(boolean showMinorMoonOrbits) { c_setShowMinorMoonOrbits(pointer, showMinorMoonOrbits); }
    private static native void c_setShowMinorMoonOrbits(long pointer, boolean showMinorMoonOrbits);
    private native boolean c_getShowMinorMoonOrbits(long pointer);

    public boolean getShowCityLabels() { return c_getShowCityLabels(pointer); }
    public void setShowCityLabels(boolean showCityLabels) { c_setShowCityLabels(pointer, showCityLabels); }
    private static native void c_setShowCityLabels(long pointer, boolean showCityLabels);
    private native boolean c_getShowCityLabels(long pointer);
    public boolean getShowObservatoryLabels() { return c_getShowObservatoryLabels(pointer); }
    public void setShowObservatoryLabels(boolean showObservatoryLabels) { c_setShowObservatoryLabels(pointer, showObservatoryLabels); }
    private static native void c_setShowObservatoryLabels(long pointer, boolean showObservatoryLabels);
    private native boolean c_getShowObservatoryLabels(long pointer);
    public boolean getShowLandingSiteLabels() { return c_getShowLandingSiteLabels(pointer); }
    public void setShowLandingSiteLabels(boolean showLandingSiteLabels) { c_setShowLandingSiteLabels(pointer, showLandingSiteLabels); }
    private static native void c_setShowLandingSiteLabels(long pointer, boolean showLandingSiteLabels);
    private native boolean c_getShowLandingSiteLabels(long pointer);
    public boolean getShowCraterLabels() { return c_getShowCraterLabels(pointer); }
    public void setShowCraterLabels(boolean showCraterLabels) { c_setShowCraterLabels(pointer, showCraterLabels); }
    private static native void c_setShowCraterLabels(long pointer, boolean showCraterLabels);
    private native boolean c_getShowCraterLabels(long pointer);
    public boolean getShowVallisLabels() { return c_getShowVallisLabels(pointer); }
    public void setShowVallisLabels(boolean showVallisLabels) { c_setShowVallisLabels(pointer, showVallisLabels); }
    private static native void c_setShowVallisLabels(long pointer, boolean showVallisLabels);
    private native boolean c_getShowVallisLabels(long pointer);
    public boolean getShowMonsLabels() { return c_getShowMonsLabels(pointer); }
    public void setShowMonsLabels(boolean showMonsLabels) { c_setShowMonsLabels(pointer, showMonsLabels); }
    private static native void c_setShowMonsLabels(long pointer, boolean showMonsLabels);
    private native boolean c_getShowMonsLabels(long pointer);
    public boolean getShowPlanumLabels() { return c_getShowPlanumLabels(pointer); }
    public void setShowPlanumLabels(boolean showPlanumLabels) { c_setShowPlanumLabels(pointer, showPlanumLabels); }
    private static native void c_setShowPlanumLabels(long pointer, boolean showPlanumLabels);
    private native boolean c_getShowPlanumLabels(long pointer);
    public boolean getShowChasmaLabels() { return c_getShowChasmaLabels(pointer); }
    public void setShowChasmaLabels(boolean showChasmaLabels) { c_setShowChasmaLabels(pointer, showChasmaLabels); }
    private static native void c_setShowChasmaLabels(long pointer, boolean showChasmaLabels);
    private native boolean c_getShowChasmaLabels(long pointer);
    public boolean getShowPateraLabels() { return c_getShowPateraLabels(pointer); }
    public void setShowPateraLabels(boolean showPateraLabels) { c_setShowPateraLabels(pointer, showPateraLabels); }
    private static native void c_setShowPateraLabels(long pointer, boolean showPateraLabels);
    private native boolean c_getShowPateraLabels(long pointer);
    public boolean getShowMareLabels() { return c_getShowMareLabels(pointer); }
    public void setShowMareLabels(boolean showMareLabels) { c_setShowMareLabels(pointer, showMareLabels); }
    private static native void c_setShowMareLabels(long pointer, boolean showMareLabels);
    private native boolean c_getShowMareLabels(long pointer);
    public boolean getShowRupesLabels() { return c_getShowRupesLabels(pointer); }
    public void setShowRupesLabels(boolean showRupesLabels) { c_setShowRupesLabels(pointer, showRupesLabels); }
    private static native void c_setShowRupesLabels(long pointer, boolean showRupesLabels);
    private native boolean c_getShowRupesLabels(long pointer);
    public boolean getShowTesseraLabels() { return c_getShowTesseraLabels(pointer); }
    public void setShowTesseraLabels(boolean showTesseraLabels) { c_setShowTesseraLabels(pointer, showTesseraLabels); }
    private static native void c_setShowTesseraLabels(long pointer, boolean showTesseraLabels);
    private native boolean c_getShowTesseraLabels(long pointer);
    public boolean getShowRegioLabels() { return c_getShowRegioLabels(pointer); }
    public void setShowRegioLabels(boolean showRegioLabels) { c_setShowRegioLabels(pointer, showRegioLabels); }
    private static native void c_setShowRegioLabels(long pointer, boolean showRegioLabels);
    private native boolean c_getShowRegioLabels(long pointer);
    public boolean getShowChaosLabels() { return c_getShowChaosLabels(pointer); }
    public void setShowChaosLabels(boolean showChaosLabels) { c_setShowChaosLabels(pointer, showChaosLabels); }
    private static native void c_setShowChaosLabels(long pointer, boolean showChaosLabels);
    private native boolean c_getShowChaosLabels(long pointer);
    public boolean getShowTerraLabels() { return c_getShowTerraLabels(pointer); }
    public void setShowTerraLabels(boolean showTerraLabels) { c_setShowTerraLabels(pointer, showTerraLabels); }
    private static native void c_setShowTerraLabels(long pointer, boolean showTerraLabels);
    private native boolean c_getShowTerraLabels(long pointer);
    public boolean getShowAstrumLabels() { return c_getShowAstrumLabels(pointer); }
    public void setShowAstrumLabels(boolean showAstrumLabels) { c_setShowAstrumLabels(pointer, showAstrumLabels); }
    private static native void c_setShowAstrumLabels(long pointer, boolean showAstrumLabels);
    private native boolean c_getShowAstrumLabels(long pointer);
    public boolean getShowCoronaLabels() { return c_getShowCoronaLabels(pointer); }
    public void setShowCoronaLabels(boolean showCoronaLabels) { c_setShowCoronaLabels(pointer, showCoronaLabels); }
    private static native void c_setShowCoronaLabels(long pointer, boolean showCoronaLabels);
    private native boolean c_getShowCoronaLabels(long pointer);
    public boolean getShowDorsumLabels() { return c_getShowDorsumLabels(pointer); }
    public void setShowDorsumLabels(boolean showDorsumLabels) { c_setShowDorsumLabels(pointer, showDorsumLabels); }
    private static native void c_setShowDorsumLabels(long pointer, boolean showDorsumLabels);
    private native boolean c_getShowDorsumLabels(long pointer);
    public boolean getShowFossaLabels() { return c_getShowFossaLabels(pointer); }
    public void setShowFossaLabels(boolean showFossaLabels) { c_setShowFossaLabels(pointer, showFossaLabels); }
    private static native void c_setShowFossaLabels(long pointer, boolean showFossaLabels);
    private native boolean c_getShowFossaLabels(long pointer);
    public boolean getShowCatenaLabels() { return c_getShowCatenaLabels(pointer); }
    public void setShowCatenaLabels(boolean showCatenaLabels) { c_setShowCatenaLabels(pointer, showCatenaLabels); }
    private static native void c_setShowCatenaLabels(long pointer, boolean showCatenaLabels);
    private native boolean c_getShowCatenaLabels(long pointer);
    public boolean getShowMensaLabels() { return c_getShowMensaLabels(pointer); }
    public void setShowMensaLabels(boolean showMensaLabels) { c_setShowMensaLabels(pointer, showMensaLabels); }
    private static native void c_setShowMensaLabels(long pointer, boolean showMensaLabels);
    private native boolean c_getShowMensaLabels(long pointer);
    public boolean getShowRimaLabels() { return c_getShowRimaLabels(pointer); }
    public void setShowRimaLabels(boolean showRimaLabels) { c_setShowRimaLabels(pointer, showRimaLabels); }
    private static native void c_setShowRimaLabels(long pointer, boolean showRimaLabels);
    private native boolean c_getShowRimaLabels(long pointer);
    public boolean getShowUndaeLabels() { return c_getShowUndaeLabels(pointer); }
    public void setShowUndaeLabels(boolean showUndaeLabels) { c_setShowUndaeLabels(pointer, showUndaeLabels); }
    private static native void c_setShowUndaeLabels(long pointer, boolean showUndaeLabels);
    private native boolean c_getShowUndaeLabels(long pointer);
    public boolean getShowReticulumLabels() { return c_getShowReticulumLabels(pointer); }
    public void setShowReticulumLabels(boolean showReticulumLabels) { c_setShowReticulumLabels(pointer, showReticulumLabels); }
    private static native void c_setShowReticulumLabels(long pointer, boolean showReticulumLabels);
    private native boolean c_getShowReticulumLabels(long pointer);
    public boolean getShowPlanitiaLabels() { return c_getShowPlanitiaLabels(pointer); }
    public void setShowPlanitiaLabels(boolean showPlanitiaLabels) { c_setShowPlanitiaLabels(pointer, showPlanitiaLabels); }
    private static native void c_setShowPlanitiaLabels(long pointer, boolean showPlanitiaLabels);
    private native boolean c_getShowPlanitiaLabels(long pointer);
    public boolean getShowLineaLabels() { return c_getShowLineaLabels(pointer); }
    public void setShowLineaLabels(boolean showLineaLabels) { c_setShowLineaLabels(pointer, showLineaLabels); }
    private static native void c_setShowLineaLabels(long pointer, boolean showLineaLabels);
    private native boolean c_getShowLineaLabels(long pointer);
    public boolean getShowFluctusLabels() { return c_getShowFluctusLabels(pointer); }
    public void setShowFluctusLabels(boolean showFluctusLabels) { c_setShowFluctusLabels(pointer, showFluctusLabels); }
    private static native void c_setShowFluctusLabels(long pointer, boolean showFluctusLabels);
    private native boolean c_getShowFluctusLabels(long pointer);
    public boolean getShowFarrumLabels() { return c_getShowFarrumLabels(pointer); }
    public void setShowFarrumLabels(boolean showFarrumLabels) { c_setShowFarrumLabels(pointer, showFarrumLabels); }
    private static native void c_setShowFarrumLabels(long pointer, boolean showFarrumLabels);
    private native boolean c_getShowFarrumLabels(long pointer);
    public boolean getShowEruptiveCenterLabels() { return c_getShowEruptiveCenterLabels(pointer); }
    public void setShowEruptiveCenterLabels(boolean showEruptiveCenterLabels) { c_setShowEruptiveCenterLabels(pointer, showEruptiveCenterLabels); }
    private static native void c_setShowEruptiveCenterLabels(long pointer, boolean showEruptiveCenterLabels);
    private native boolean c_getShowEruptiveCenterLabels(long pointer);
    public boolean getShowOtherLabels() { return c_getShowOtherLabels(pointer); }
    public void setShowOtherLabels(boolean showOtherLabels) { c_setShowOtherLabels(pointer, showOtherLabels); }
    private static native void c_setShowOtherLabels(long pointer, boolean showOtherLabels);
    private native boolean c_getShowOtherLabels(long pointer);

    public void setResolution(int resolution) { c_setResolution(pointer, resolution); }
    public int getResolution() { return c_getResolution(pointer); }
    private static native void c_setResolution(long pointer, int resolution);
    private static native int c_getResolution(long pointer);
    public void setStarStyle(int starStyle) { c_setStarStyle(pointer, starStyle);}
    public int getStarStyle() { return c_getStarStyle(pointer); }
    private static native void c_setStarStyle(long pointer, int starStyle);
    private static native int c_getStarStyle(long pointer);

    public void setStarColors(int starColors) { c_setStarColors(pointer, starColors);}
    public int getStarColors() { return c_getStarColors(pointer); }
    private static native void c_setStarColors(long pointer, int starColors);
    private static native int c_getStarColors(long pointer);

    public void setTintSaturation(double tintSaturation) { c_setTintSaturation(pointer, (float)tintSaturation);}
    public double getTintSaturation() { return c_getTintSaturation(pointer); }
    private static native void c_setTintSaturation(long pointer, float tintSaturation);
    private static native float c_getTintSaturation(long pointer);
    public void setHudDetail(int hudDetail) { c_setHudDetail(pointer, hudDetail); }
    public int getHudDetail() { return c_getHudDetail(pointer); }
    private static native void c_setHudDetail(long pointer, int hudDetail);
    private static native int c_getHudDetail(long pointer);
    public void setMeasurementSystem(int measurementSystem) { c_setMeasurementSystem(pointer, measurementSystem); }
    public int getMeasurementSystem() { return c_getMeasurementSystem(pointer); }
    private static native void c_setMeasurementSystem(long pointer, int measurementSystem);
    private static native int c_getMeasurementSystem(long pointer);
    public void setTemperatureScale(int temperatureScale) { c_setTemperatureScale(pointer, temperatureScale); }
    public int getTemperatureScale() { return c_getTemperatureScale(pointer); }
    private static native void c_setTemperatureScale(long pointer, int temperatureScale);
    private static native int c_getTemperatureScale(long pointer);

    public void setScriptSystemAccessPolicy(int scriptSystemAccessPolicy) { c_setScriptSystemAccessPolicy(pointer, scriptSystemAccessPolicy); }
    public int getScriptSystemAccessPolicy() { return c_getScriptSystemAccessPolicy(pointer); }
    private static native void c_setScriptSystemAccessPolicy(long pointer, int scriptSystemAccessPolicy);
    private static native int c_getScriptSystemAccessPolicy(long pointer);

    public void setLayoutDirection(int layoutDirection) { c_setLayoutDirection(pointer, layoutDirection); }
    public int getLayoutDirection() { return c_getLayoutDirection(pointer); }

    private static native int c_getLayoutDirection(long ptr);
    private static native void c_setLayoutDirection(long ptr, int layoutDirection);

    public void setTimeZone(int timeZone) { c_setTimeZone(pointer, timeZone); }
    public int getTimeZone() { return c_getTimeZone(pointer); }
    private static native void c_setTimeZone(long pointer, int TimeZone);
    private static native int c_getTimeZone(long pointer);
    public void setDateFormat(int dateFormat) { c_setDateFormat(pointer, dateFormat); }
    public int getDateFormat() { return c_getDateFormat(pointer); }
    private static native void c_setDateFormat(long pointer, int DateFormat);
    private static native int c_getDateFormat(long pointer);

    public void setAmbientLightLevel(double ambientLightLevel) { c_setAmbientLightLevel(pointer, ambientLightLevel); }
    public double getAmbientLightLevel() { return c_getAmbientLightLevel(pointer); }
    private static native void c_setAmbientLightLevel(long pointer, double ambientLightLevel);
    private static native double c_getAmbientLightLevel(long pointer);

    public void setFaintestVisible(double faintestVisible) { c_setFaintestVisible(pointer, faintestVisible); }
    public double getFaintestVisible() { return c_getFaintestVisible(pointer); }
    private static native void c_setFaintestVisible(long pointer, double faintestVisible);
    private static native double c_getFaintestVisible(long pointer);

    public void setGalaxyBrightness(double galaxyBrightness) { c_setGalaxyBrightness(pointer, galaxyBrightness); }
    public double getGalaxyBrightness() { return c_getGalaxyBrightness(pointer); }
    private static native void c_setGalaxyBrightness(long pointer, double galaxyBrightness);
    private static native double c_getGalaxyBrightness(long pointer);

    public void setMinimumFeatureSize(double minimumFeatureSize) { c_setMinimumFeatureSize(pointer, minimumFeatureSize); }
    public double getMinimumFeatureSize() { return c_getMinimumFeatureSize(pointer); }
    private static native void c_setMinimumFeatureSize(long pointer, double minimumFeatureSize);
    private static native double c_getMinimumFeatureSize(long pointer);

    public boolean getShowBodyAxes() { return c_getReferenceMarkEnabled(pointer, "body axes"); }
    public void setShowBodyAxes(boolean value) { c_toggleReferenceMarkEnabled(pointer, "body axes"); }

    public boolean getShowFrameAxes() { return c_getReferenceMarkEnabled(pointer, "frame axes"); }
    public void setShowFrameAxes(boolean value) { c_toggleReferenceMarkEnabled(pointer, "frame axes"); }

    public boolean getShowSunDirection() { return c_getReferenceMarkEnabled(pointer, "sun direction"); }
    public void setShowSunDirection(boolean value) { c_toggleReferenceMarkEnabled(pointer, "sun direction"); }

    public boolean getShowVelocityVector() { return c_getReferenceMarkEnabled(pointer, "velocity vector"); }
    public void setShowVelocityVector(boolean value) { c_toggleReferenceMarkEnabled(pointer, "velocity vector"); }

    public boolean getShowPlanetographicGrid() { return c_getReferenceMarkEnabled(pointer, "planetographic grid"); }
    public void setShowPlanetographicGrid(boolean value) { c_toggleReferenceMarkEnabled(pointer, "planetographic grid"); }

    public boolean getShowTerminator() { return c_getReferenceMarkEnabled(pointer, "terminator"); }
    public void setShowTerminator(boolean value) { c_toggleReferenceMarkEnabled(pointer, "terminator"); }

    private static native boolean c_getReferenceMarkEnabled(long ptr, String str);
    private static native void c_toggleReferenceMarkEnabled(long ptr, String str);
}
