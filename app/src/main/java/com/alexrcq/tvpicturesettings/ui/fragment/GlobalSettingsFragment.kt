package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.helper.GlobalSettings
import com.alexrcq.tvpicturesettings.ui.preference.GlobalListPreferences
import com.alexrcq.tvpicturesettings.ui.preference.GlobalSeekbarPreference

open class GlobalSettingsFragment(@XmlRes private val preferencesResId: Int) :
    BasePreferenceFragment(preferencesResId) {

    lateinit var contentResolver: ContentResolver

    private val contentObserver: ContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                val key = uri?.lastPathSegment
                if (key != null) {
                    onGlobalSettingChanged(key)
                }
            }
        }

    lateinit var globalSettings: GlobalSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentResolver = requireContext().contentResolver
        globalSettings = (requireActivity().application as App).globalSettings
        globalSettings.registerContentObserver(contentObserver)
    }

    override fun onStart() {
        super.onStart()
        preferenceScreen.forEach { preference ->
            updatePreference(preference)
        }
    }

    private fun onGlobalSettingChanged(key: String) {
        val preference = findPreference<Preference>(key)
        if (preference != null) {
            updatePreference(preference)
        }
    }

    @CallSuper
    open fun updatePreference(preference: Preference) {
        when (preference) {
            is GlobalSeekbarPreference -> preference.value = globalSettings.getInt(preference.key)
            is GlobalListPreferences -> preference.value =
                globalSettings.getInt(preference.key).toString()
        }
    }

    @CallSuper
    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference) {
            is GlobalSeekbarPreference -> globalSettings.putInt(preference.key, newValue as Int)
            is GlobalListPreferences -> globalSettings.putInt(
                preference.key,
                (newValue as String).toInt()
            )
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentResolver.unregisterContentObserver(contentObserver)
    }
}