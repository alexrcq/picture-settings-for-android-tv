package com.alexrcq.tvpicturesettings.platform.android

import android.provider.Settings
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore

class SystemDeveloperSettings(private val globalSettings: GlobalSettingsStore): DeveloperSettings {

    override var isAdbEnabled: Boolean
        get() = globalSettings.getBoolean(Settings.Global.ADB_ENABLED, false)
        set(value) {
            globalSettings.putBoolean(Settings.Global.ADB_ENABLED, value)
        }
}