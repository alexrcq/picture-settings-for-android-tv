package com.alexrcq.tvpicturesettings.util

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings

class GlobalSettingsObserver : ContentObserver(Handler(Looper.getMainLooper())) {

    private var callback: OnGlobalSettingChangedCallback? = null
    private var contentResolver: ContentResolver? = null
    private var isObserving = false

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.lastPathSegment?.let {
            callback?.onGlobalSettingChanged(it)
        }
    }

    fun observe(contentResolver: ContentResolver, callback: OnGlobalSettingChangedCallback) {
        if (!isObserving) {
            this.contentResolver = contentResolver
            this.callback = callback
            contentResolver.registerContentObserver(
                Settings.Global.CONTENT_URI,
                true,
                this
            )
            isObserving = true
        }
    }

    fun stopObserving() {
        if (isObserving) {
            contentResolver?.unregisterContentObserver(this)
            isObserving = false
        }
    }
}