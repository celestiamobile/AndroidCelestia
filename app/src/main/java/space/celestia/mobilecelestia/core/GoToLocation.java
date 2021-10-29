/*
 * CelestiaGoToLocation.java
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.core;

import androidx.annotation.NonNull;

public class GoToLocation {
    public enum DistanceUnit {
        km, radii, au
    }

    protected static final int FieldMaskLongitude = 1 << 0;
    protected static final int FieldMaskLatitude = 1 << 1;
    protected static final int FieldMaskDistance = 1 << 2;

    protected final Selection selection;
    protected final float longitude;
    protected final float latitude;
    protected final double distance;
    protected final double duration;
    protected final int fieldMask;

    public GoToLocation(@NonNull Selection selection, float longitude, float latitude, double distance, DistanceUnit unit) {
        this.selection = selection;
        this.longitude = longitude;
        this.latitude = latitude;
        this.duration = 5;

        switch (unit)
        {
            case au:
                this.distance = Utils.AUToKilometers(distance);
                break;
            case km:
                this.distance = distance;
                break;
            case radii:
            default:
                this.distance = distance * selection.getRadius();
                break;
        }

        fieldMask = FieldMaskLongitude | FieldMaskLatitude | FieldMaskDistance;
    }

    public GoToLocation(@NonNull Selection selection, float longitude, float latitude) {
        this.selection = selection;
        this.longitude = longitude;
        this.latitude = latitude;
        this.distance = 0;
        this.duration = 5;
        fieldMask = FieldMaskLongitude | FieldMaskLatitude;
    }

    public GoToLocation(@NonNull Selection selection, double distance, DistanceUnit unit) {
        this.selection = selection;
        this.duration = 5;

        this.longitude = 0;
        this.latitude = 0;

        switch (unit)
        {
            case au:
                this.distance = Utils.AUToKilometers(distance);
                break;
            case km:
                this.distance = distance;
                break;
            case radii:
            default:
                this.distance = distance * selection.getRadius();
                break;
        }

        fieldMask = FieldMaskDistance;
    }

    public GoToLocation(@NonNull Selection selection) {
        this.selection = selection;
        this.duration = 5;

        this.longitude = 0;
        this.latitude = 0;
        this.distance = 0;

        fieldMask = 0;
    }
}
