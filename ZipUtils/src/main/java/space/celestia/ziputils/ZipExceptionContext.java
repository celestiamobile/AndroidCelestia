// ZipExceptionContext.java
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.ziputils;

import org.jetbrains.annotations.Nullable;

public class ZipExceptionContext {
    public static final int ZIP_ERROR = 1;
    public static final int CREATE_DIRECTORY_ERROR = 2;
    public static final int OPEN_FILE_ERROR = 3;
    public static final int WRITE_FILE_ERROR = 4;


    public final int code;
    public final @Nullable String contextPath;

    ZipExceptionContext(int code, @Nullable String contextPath) {
        this.code = code;
        this.contextPath = contextPath;
    }
}
