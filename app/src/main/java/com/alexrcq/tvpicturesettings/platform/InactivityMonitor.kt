package com.alexrcq.tvpicturesettings.platform

interface InactivityMonitor {
    suspend fun startMonitoring(timeoutMinutes: Long, onTimeout: () -> Unit)
}