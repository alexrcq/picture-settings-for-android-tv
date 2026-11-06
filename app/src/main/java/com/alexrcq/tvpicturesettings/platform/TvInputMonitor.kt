package com.alexrcq.tvpicturesettings.platform

import kotlinx.coroutines.flow.Flow

interface TvInputMonitor {
    val activeInput: Flow<String?>
}