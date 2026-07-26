package com.alexrcq.tvpicturesettings.ui.fragment.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference

abstract class BasePreferenceFragment(@XmlRes private val preferencesResId: Int) : LeanbackPreferenceFragmentCompat() {

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (preferencesResId != 0) {
            setPreferencesFromResource(preferencesResId, rootKey)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.fragment == null && preference.key != null) {
            val callback = parentFragment as? OnPreferenceStartFragmentCallback
            if (callback != null && callback.onPreferenceStartFragment(this, preference)) {
                return true
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}