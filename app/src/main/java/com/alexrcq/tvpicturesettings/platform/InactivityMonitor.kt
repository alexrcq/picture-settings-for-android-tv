package com.alexrcq.tvpicturesettings.platform

import kotlinx.coroutines.flow.Flow

interface InactivityMonitor {
    val idleDurationMs: Flow<Long>
}