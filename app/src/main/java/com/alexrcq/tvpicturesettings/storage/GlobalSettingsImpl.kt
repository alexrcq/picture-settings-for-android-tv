package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings

open class GlobalSettingsImpl(private val contentResolver: ContentResolver) : GlobalSettings {

    private var settingsChangedCallbacks = mutableSetOf<OnGlobalSettingChangedCallback>()
    private var contentObserver: ContentObserver? = null

    override fun putInt(key: String, value: Int) {
        Settings.Global.putInt(contentResolver, key, value)
    }

    override fun getInt(key: String): Int = Settings.Global.getInt(contentResolver, key)

    fun addOnSettingsChangedCallback(callback: OnGlobalSettingChangedCallback) {
        settingsChangedCallbacks.add(callback)
        if (contentObserver == null) {
            contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    val key = uri?.lastPathSegment!!
                    settingsChangedCallbacks.forEach {
                        it.onGlobalSettingChanged(key, getInt(key))
                    }
                }
            }
            contentResolver.registerContentObserver(
                Settings.Global.CONTENT_URI,
                true,
                contentObserver!!
            )
        }
    }

    fun removeOnSettingsChangedCallback(callback: OnGlobalSettingChangedCallback) {
        settingsChangedCallbacks.remove(callback)
    }

    fun interface OnGlobalSettingChangedCallback {
        fun onGlobalSettingChanged(key: String, value: Int)
    }
}