/*
 * SubscriptionManagerViewModel.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.purchase.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.mobilecelestia.purchase.PurchaseManager
import javax.inject.Inject

@HiltViewModel
class SubscriptionManagerViewModel @Inject constructor(val purchaseManager: PurchaseManager) : ViewModel()