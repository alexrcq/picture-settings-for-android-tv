package com.alexrcq.tvpicturesettings.util

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class GlobalSettingsObserverImpl : GlobalSettingsObserver, LifecycleEventObserver {

    private lateinit var contentResolver: ContentResolver
    private lateinit var contentObserver: ContentObserver

    override fun registerGlobalSettingsObserver(
        lifecycleOwner: LifecycleOwner,
        contentResolver: ContentResolver,
        onGlobalSettingChangedCallback: GlobalSettingsObserver.OnGlobalSettingChangedCallback
    ) {
        this.contentResolver = contentResolver
        this.contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                val key = uri?.lastPathSegment
                if (key != null) {
                    onGlobalSettingChangedCallback.onGlobalSettingChanged(key)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> registerContentObserver()
            Lifecycle.Event.ON_STOP -> unregisterContentObserver()
            else -> Unit
        }
    }

    private fun registerContentObserver() {
        contentResolver.registerContentObserver(
            Settings.Global.CONTENT_URI,
            true,
            contentObserver
        )
    }

    private fun unregisterContentObserver() {
        contentResolver.unregisterContentObserver(contentObserver)
    }
}

