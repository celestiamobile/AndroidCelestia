/*
 * CelestiaViewModel.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.mobilecelestia.celestia.CelestiaControlButton
import javax.inject.Inject

@HiltViewModel
class CelestiaViewModel @Inject constructor() : ViewModel() {
    var controlButtons: List<CelestiaControlButton> by mutableStateOf(listOf())
}