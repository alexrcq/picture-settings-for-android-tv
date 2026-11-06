package com.alexrcq.tvpicturesettings.platform.android

import android.provider.Settings
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore

class DeveloperSettings(private val globalSettings: GlobalSettingsStore) {

    var isAdbEnabled: Boolean
        get() = globalSettings.getBoolean(Settings.Global.ADB_ENABLED, false)
        set(value) {
            globalSettings.putBoolean(Settings.Global.ADB_ENABLED, value)
        }
}
