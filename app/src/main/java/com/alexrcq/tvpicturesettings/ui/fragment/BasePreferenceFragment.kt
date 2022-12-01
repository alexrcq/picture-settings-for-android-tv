package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.forEach

abstract class BasePreferenceFragment(@XmlRes private val preferencesResId: Int) :
    LeanbackPreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferencesResId, rootKey)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    fun <T: Preference> requirePreference(key: String): T {
        val preference = findPreference<T>(key)
        return checkNotNull(preference) { "Preference $key not found" }
    }
}