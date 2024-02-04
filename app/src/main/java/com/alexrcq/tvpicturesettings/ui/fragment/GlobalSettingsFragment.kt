package com.alexrcq.tvpicturesettings.ui.fragment

import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.ui.preference.GlobalListPreferences
import com.alexrcq.tvpicturesettings.ui.preference.GlobalSeekbarPreference

abstract class GlobalSettingsFragment(@XmlRes private val prefsResId: Int) : BasePreferenceFragment(prefsResId),
    OnPreferenceChangeListener {

    private val contentObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val key = uri?.lastPathSegment
            if (key != null) {
                findPreference<Preference>(key)?.let { updatePreference(it) }
            }
        }
    }

    lateinit var globalSettings: GlobalSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalSettings = (requireActivity().application as App).tvSettings.global
        globalSettings.registerContentObserver(contentObserver)
        preferenceScreen.forEach { preference -> preference.onPreferenceChangeListener = this }
    }

    override fun onStart() {
        super.onStart()
        preferenceScreen.forEach { preference ->
            updatePreference(preference)
        }
    }

    @CallSuper
    open fun updatePreference(preference: Preference) {
        when (preference) {
            is GlobalSeekbarPreference -> preference.value = globalSettings.getInt(preference.key)
            is GlobalListPreferences -> preference.value = globalSettings.getInt(preference.key).toString()
        }
    }

    @CallSuper
    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference) {
            is GlobalSeekbarPreference -> globalSettings.putInt(preference.key, newValue as Int)
            is GlobalListPreferences -> globalSettings.putInt(preference.key, (newValue as String).toInt())
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        globalSettings.unregisterContentObserver(contentObserver)
    }
}