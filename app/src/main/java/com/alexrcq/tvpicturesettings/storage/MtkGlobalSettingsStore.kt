package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver

class MtkGlobalSettingsStore(contentResolver: ContentResolver) : GlobalSettingsStore(contentResolver) {

    override fun getBoolean(key: String, defValue: Boolean): Boolean = when (key) {
        MtkGlobalKeys.PICTURE_LOCAL_CONTRAST, MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL -> {
            getInt(key, if (defValue) 2 else 0) == 2
        }
        else -> super.getBoolean(key, defValue)
    }

    override fun putBoolean(key: String, value: Boolean) {
        when (key) {
            MtkGlobalKeys.PICTURE_LOCAL_CONTRAST, MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL -> {
                putInt(key, if (value) 2 else 0)
            }
            else -> super.putBoolean(key, value)
        }
    }
}