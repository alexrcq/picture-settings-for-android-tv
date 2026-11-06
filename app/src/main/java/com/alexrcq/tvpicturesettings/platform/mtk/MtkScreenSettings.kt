package com.alexrcq.tvpicturesettings.platform.mtk

import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys

class MtkScreenSettings(private val globalSettings: GlobalSettingsStore) : ScreenSettings {

    override val maxBacklight: Int = 100

    override var backlight: Int
        get() = globalSettings.getInt(MtkGlobalKeys.PICTURE_BACKLIGHT, 0)
        set(value) {
            globalSettings.putInt(MtkGlobalKeys.PICTURE_BACKLIGHT, value)
        }

    override var isScreenOn: Boolean
        get() = globalSettings.getBoolean(MtkGlobalKeys.POWER_PICTURE_OFF, true)
        set(value) {
            globalSettings.putBoolean(MtkGlobalKeys.POWER_PICTURE_OFF, value)
        }

    override fun toggleScreenPower() {
        isScreenOn = !isScreenOn
    }
}