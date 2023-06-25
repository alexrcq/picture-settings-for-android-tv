package com.alexrcq.tvpicturesettings.helper

import android.content.SharedPreferences
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper

class SettingsObserver(
    val appSettings: AppSettings,
    val globalSettings: GlobalSettings,
    var onSettingChanged: ((key: String) -> Unit)
) {

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val key = uri?.lastPathSegment
            if (key != null) {
                onSettingChanged.invoke(key)
            }
        }
    }
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        onSettingChanged.invoke(key)
    }

    fun observe() {
        appSettings.registerOnSharedPreferenceChangeListener(prefsListener)
        globalSettings.registerContentObserver(contentObserver)
    }

    fun stopObserving() {
        appSettings.unregisterOnSharedPreferenceChangeListener(prefsListener)
        globalSettings.unregisterContentObserver(contentObserver)
    }
}