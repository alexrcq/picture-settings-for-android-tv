package com.alexrcq.tvpicturesettings.helper

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.Settings
import timber.log.Timber

open class GlobalSettingsWrapper(private val contentResolver: ContentResolver) : GlobalSettings {

    override fun putInt(key: String, value: Int) {
        Timber.d("putInt : key = $key, value = $value")
        Settings.Global.putInt(contentResolver, key, value)
    }

    override fun getInt(key: String): Int {
        val value = Settings.Global.getInt(contentResolver, key)
        Timber.d("getInt : key = $key, value = $value")
        return value
    }

    override fun getInt(key: String, def: Int): Int {
        val value = Settings.Global.getInt(contentResolver, key, def)
        Timber.d("getInt : key = $key, value = $value")
        return value
    }

    override fun registerContentObserver(observer: ContentObserver) {
        contentResolver.registerContentObserver(Settings.Global.CONTENT_URI, true, observer)
    }

    override fun unregisterContentObserver(observer: ContentObserver) {
        contentResolver.unregisterContentObserver(observer)
    }
}