/*
 * build.gradle.kts
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

plugins {
    alias(libs.plugins.asset.pack)
}

assetPack {
    packName = "base_assets"
    dynamicDelivery {
        deliveryType = "install-time"
    }
}