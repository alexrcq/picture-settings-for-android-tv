package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver
import android.provider.Settings

open class GlobalSettingsImpl(private val contentResolver: ContentResolver) : GlobalSettings {
    override fun putInt(key: String, value: Int) {
        Settings.Global.putInt(contentResolver, key, value)
    }
    override fun getInt(key: String): Int = Settings.Global.getInt(contentResolver, key)
}