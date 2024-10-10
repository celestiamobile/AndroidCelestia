/*
 * Completion.java
 *
 * Copyright (C) 2024-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestia;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Completion implements Parcelable {
    @NonNull
    public final String name;
    @NonNull
    public final Selection selection;

    private Completion(@NonNull String name, @NonNull Selection selection) {
        this.name = name;
        this.selection = selection;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(selection, 0);
    }

    public static final Creator<Completion> CREATOR = new Creator<>() {
        @Override
        public Completion createFromParcel(Parcel in) {
            String name = in.readString();
            Selection selection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                selection = in.readParcelable(Selection.class.getClassLoader(), Selection.class);
            } else {
                selection = in.readParcelable(Selection.class.getClassLoader());
            }
            assert name != null;
            assert selection != null;
            return new Completion(name, selection);
        }

        @Override
        public Completion[] newArray(int size) {
            return new Completion[size];
        }
    };
}
