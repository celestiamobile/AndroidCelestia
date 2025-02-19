/*
 * GoToViewModel.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.CelestiaExecutor
import javax.inject.Inject

@HiltViewModel
class GoToViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel()