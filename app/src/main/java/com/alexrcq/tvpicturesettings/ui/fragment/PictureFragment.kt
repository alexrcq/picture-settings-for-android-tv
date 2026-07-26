package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.BuildConfig
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.platform.Device
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.ui.preference.LongPressGlobalSeekBarPreference
import com.alexrcq.tvpicturesettings.ui.viewmodel.PictureViewModel
import com.alexrcq.tvpicturesettings.util.onClick
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.launch

class PictureFragment : GlobalSettingsFragment(R.xml.picture_prefs) {

    private var backlightPref: SeekBarPreference? = null

    private val viewModel: PictureViewModel by viewModels {
        with(requireActivity().application as App) {
            PictureViewModel.provideFactory(
                screenSettings,
                darkModeManager,
                darkModePreferences,
                backlightControlPolicy
            )
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        backlightPref = findPreference<LongPressGlobalSeekBarPreference>(MtkGlobalKeys.PICTURE_BACKLIGHT)?.apply {
            onClick { viewModel.toggleDarkMode() }
            onLongClick { viewModel.toggleFullDarkMode() }
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.onBacklightChange(newValue as Int)
                true
            }
        }
        findPreference<Preference>(PreferencesKeys.TOGGLE_SCREEN_POWER)?.onClick {
            viewModel.toggleScreenPower()
        }
        findPreference<Preference>(PreferencesKeys.MSSP_VIDEO_PREFERENCES)?.apply {
            if (!Device.isMssp) {
                title = getString(R.string.picture_settings)
                onClick(::openVendorPictureSettings)
            }
        }
        findPreference<Preference>(PreferencesKeys.APP_DESCRIPTION)?.summary =
            getString(R.string.app_description, BuildConfig.VERSION_NAME)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.darkModeHints.collect { hintResId ->
                        backlightPref?.summary = getString(hintResId)
                    }
                }
                launch {
                    viewModel.canAdjustBacklight.collect { canAdjustBacklight ->
                        backlightPref?.isEnabled = canAdjustBacklight
                    }
                }
            }
        }
    }

    private fun openVendorPictureSettings() {
        if (Device.is4kSmartTv) {
            openPqaqSettings()
        } else {
            openPictureSettings()
        }
    }

    private fun openPictureSettings() {
        val intent = Intent().apply {
            component = ComponentName(TvConstants.TV_SETTINGS_PACKAGE, TvConstants.TV_PICTURE_ACTIVITY_NAME)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast(getString(R.string.activity_not_found, TvConstants.TV_PICTURE_ACTIVITY_NAME))
        }
    }

    private fun openPqaqSettings() {
        try {
            startActivity(Intent(TvConstants.ACTION_PQAQ_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            showToast("No activity for the action '${TvConstants.ACTION_PQAQ_SETTINGS}'")
        }
    }
}