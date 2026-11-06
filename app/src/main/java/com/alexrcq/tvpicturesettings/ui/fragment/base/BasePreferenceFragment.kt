package com.alexrcq.tvpicturesettings.ui.fragment.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.XmlRes
import androidx.core.os.bundleOf
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference

open class BasePreferenceFragment protected constructor(
    @XmlRes private val preferencesResId: Int
) : LeanbackPreferenceFragmentCompat() {

    constructor() : this(0)

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val resId = arguments?.getInt(ARG_PREFS_RES_ID, preferencesResId) ?: preferencesResId
        if (resId != 0) {
            setPreferencesFromResource(resId, rootKey)
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

    companion object {
        private const val ARG_PREFS_RES_ID = "prefs_res_id"

        fun newInstance(@XmlRes preferencesResId: Int) = BasePreferenceFragment().apply {
            arguments = bundleOf(ARG_PREFS_RES_ID to preferencesResId)
        }
    }
}
