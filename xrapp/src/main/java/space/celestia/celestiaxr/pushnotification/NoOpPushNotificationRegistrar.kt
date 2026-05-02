/*
 * NoOpPushNotificationRegistrar.kt
 *
 * Copyright (C) 2026-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestiaxr.pushnotification

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.celestia.celestiaui.pushnotification.PushNotificationRegistrar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpPushNotificationRegistrar @Inject constructor() : PushNotificationRegistrar {
    override suspend fun register() {}
}

@Module
@InstallIn(SingletonComponent::class)
interface PushNotificationRegistrarModule {
    @Binds
    fun bindPushNotificationRegistrar(impl: NoOpPushNotificationRegistrar): PushNotificationRegistrar
}
