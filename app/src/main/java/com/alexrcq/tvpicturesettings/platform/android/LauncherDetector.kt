package com.alexrcq.tvpicturesettings.platform.android

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import kotlin.time.Duration.Companion.hours

private val USAGE_EVENTS_WINDOW_MS = 24.hours.inWholeMilliseconds

fun Context.isLauncherForeground(): Boolean = runCatching {
    val usageStatsManager = getSystemService(UsageStatsManager::class.java) ?: return@runCatching false
    val now = System.currentTimeMillis()
    val events = usageStatsManager.queryEvents(now - USAGE_EVENTS_WINDOW_MS, now) ?: return@runCatching false
    val event = UsageEvents.Event()
    var foregroundPackage: String? = null
    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            foregroundPackage = event.packageName
        }
    }
    if (foregroundPackage == null) return@runCatching false

    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_HOME)
        .setPackage(foregroundPackage)
    @Suppress("DEPRECATION")
    packageManager.queryIntentActivities(intent, 0).isNotEmpty()
}.getOrDefault(false)
