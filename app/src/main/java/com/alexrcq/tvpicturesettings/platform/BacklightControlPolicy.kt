package com.alexrcq.tvpicturesettings.platform

import kotlinx.coroutines.flow.StateFlow

interface BacklightControlPolicy {
    val isAdjustmentAllowed: StateFlow<Boolean>
}