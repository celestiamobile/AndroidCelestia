/*
 * LPLinkMetadata.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.ui.linkpreview

import android.graphics.Bitmap
import java.io.Serializable
import java.net.URL

class LPLinkMetadata(val url: URL, val originalURL: URL, val title: String, val imageURL: URL?, val iconURL: URL) : Serializable
class LPLinkViewData(val url: URL, val title: String, val image: Bitmap?, val usesIcon: Boolean)