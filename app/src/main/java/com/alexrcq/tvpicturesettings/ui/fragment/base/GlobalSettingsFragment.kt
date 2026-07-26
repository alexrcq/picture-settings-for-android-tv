package com.alexrcq.tvpicturesettings.ui.fragment.base

import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsStore

abstract class GlobalSettingsFragment(@XmlRes prefsResId: Int) : BasePreferenceFragment(prefsResId) {

    protected lateinit var globalSettings: GlobalSettingsStore

    private val contentObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val key = uri?.lastPathSegment ?: return
            findPreference<Preference>(key)?.let { onGlobalPreferenceChanged(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalSettings = GlobalSettingsStore(requireContext().contentResolver)
        context?.contentResolver?.registerContentObserver(Settings.Global.CONTENT_URI, true, contentObserver)
    }

    @CallSuper
    open fun onGlobalPreferenceChanged(preference: Preference) {
        val store = preference.preferenceDataStore ?: globalSettings
        when (preference) {
            is SeekBarPreference -> {
                preference.value = store.getInt(preference.key, 0)
            }
            is TwoStatePreference -> {
                preference.isChecked = store.getBoolean(preference.key, false)
            }
            is ListPreference -> {
                preference.value = store.getString(preference.key, null)
            }
        }
    }

    override fun onDestroy() {
        context?.contentResolver?.unregisterContentObserver(contentObserver)
        super.onDestroy()
    }
}