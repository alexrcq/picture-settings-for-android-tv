package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver
import android.provider.Settings
import timber.log.Timber

open class GlobalSettingsWrapper(private val contentResolver: ContentResolver): GlobalSettings {

    override fun putInt(key: String, value: Int) {
        Timber.d("putInt : key = $key, value = $value")
        Settings.Global.putInt(contentResolver, key, value)
    }

    override fun getInt(key: String): Int {
        val value = Settings.Global.getInt(contentResolver, key)
        Timber.d("getInt : key = $key, value = $value")
        return value
    }
}