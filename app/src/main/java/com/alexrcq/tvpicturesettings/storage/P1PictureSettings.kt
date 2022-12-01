package com.alexrcq.tvpicturesettings.storage

import android.content.Context
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_ADAPTIVE_LUMA_CONTROL
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_LOCAL_CONTRAST
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_TEMPERATURE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class P1PictureSettings @Inject constructor(@ApplicationContext context: Context) :
    PictureSettingsImpl(context) {

    override var isAdaptiveLumaEnabled: Boolean
        set(value) {
            val enabled = if (value) 2 else 0
            putInt(PICTURE_ADAPTIVE_LUMA_CONTROL, enabled)
        }
        get() {
            return when (getInt(PICTURE_ADAPTIVE_LUMA_CONTROL)) {
                2 -> true
                else -> false
            }
        }

    override var isLocalContrastEnabled: Boolean
        set(value) {
            val enabled = if (value) 2 else 0
            putInt(PICTURE_LOCAL_CONTRAST, enabled)
        }
        get() {
            return when (getInt(PICTURE_LOCAL_CONTRAST)) {
                2 -> true
                else -> false
            }
        }

    override var temperature: Int
        set(value) {
            putInt(PICTURE_TEMPERATURE, convertToP1Temperature(value))
        }
        get() = convertToP1Temperature(getInt(PICTURE_TEMPERATURE))

    private fun convertToP1Temperature(temperature: Int): Int {
        return when (temperature) {
            1 -> 2
            2 -> 1
            else -> 3
        }
    }
}