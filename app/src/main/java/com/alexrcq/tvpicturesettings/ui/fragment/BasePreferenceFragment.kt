package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }
}