package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment
import kotlinx.coroutines.launch

class DarkModePreferencesFragment : BasePreferenceFragment(R.xml.dark_mode_prefs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backlightControlPolicy = (requireActivity().application as App).backlightControlPolicy
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                backlightControlPolicy.adjustmentAllowedFlow.collect { isAdjustmentAllowed ->
                    findPreference<Preference>(PreferencesKeys.NIGHT_BACKLIGHT)?.isEnabled = isAdjustmentAllowed
                }
            }
        }
    }
}