package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.forEach
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.helper.AppSettings

abstract class BasePreferenceFragment(@XmlRes private val preferencesResId: Int) :
    LeanbackPreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    lateinit var appSettings: AppSettings

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferencesResId, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appSettings = (requireActivity().application as App).appSettings
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    fun <T: Preference> requirePreference(key: String): T {
        val preference = findPreference<T>(key)
        return checkNotNull(preference) { "Preference $key not found" }
    }
}