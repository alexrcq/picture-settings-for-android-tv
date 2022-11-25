package com.alexrcq.tvpicturesettings.helper

import android.content.ContentResolver
import androidx.lifecycle.LifecycleOwner


interface GlobalSettingsObserver {
    fun registerGlobalSettingsObserver(
        lifecycleOwner: LifecycleOwner,
        contentResolver: ContentResolver,
        onGlobalSettingChangedCallback: OnGlobalSettingChangedCallback
    )

    interface OnGlobalSettingChangedCallback {
        fun onGlobalSettingChanged(key: String)
    }
}