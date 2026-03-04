// AppState.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestia;

import androidx.annotation.NonNull;

public class AppState {
    public final float speed;
    public final double timeScale;
    public final double time;
    public final boolean isPaused;
    public final boolean isLightTravelDelayEnabled;

    public final boolean showDistanceToSelection;
    public final boolean showDistanceToSelectionCenter;
    public final double distanceToSelectionSurface;
    public final double distanceToSelectionCenter;

    public final int coordinateSystem;
    public final @NonNull Selection referenceObject;
    public final @NonNull Selection targetObject;
    public final @NonNull Selection selectedObject;

    // Called from JNI
    public AppState(float speed, double timeScale, double time, boolean isPaused,
                    boolean isLightTravelDelayEnabled,
                    boolean showDistanceToSelection, boolean showDistanceToSelectionCenter,
                    double distanceToSelectionSurface, double distanceToSelectionCenter,
                    int coordinateSystem,
                    @NonNull Selection referenceObject, @NonNull Selection targetObject,
                    @NonNull Selection selectedObject) {
        this.speed = speed;
        this.timeScale = timeScale;
        this.time = time;
        this.isPaused = isPaused;
        this.isLightTravelDelayEnabled = isLightTravelDelayEnabled;
        this.showDistanceToSelection = showDistanceToSelection;
        this.showDistanceToSelectionCenter = showDistanceToSelectionCenter;
        this.distanceToSelectionSurface = distanceToSelectionSurface;
        this.distanceToSelectionCenter = distanceToSelectionCenter;
        this.coordinateSystem = coordinateSystem;
        this.referenceObject = referenceObject;
        this.targetObject = targetObject;
        this.selectedObject = selectedObject;
    }
}
