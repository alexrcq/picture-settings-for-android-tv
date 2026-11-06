package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.BuildConfig
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.manager.DarkModeManager
import com.alexrcq.tvpicturesettings.manager.model.DarkMode
import com.alexrcq.tvpicturesettings.platform.BacklightControlPolicy
import com.alexrcq.tvpicturesettings.platform.Device
import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.ui.preference.LongPressGlobalSeekBarPreference
import com.alexrcq.tvpicturesettings.util.onClick
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

class PictureFragment : GlobalSettingsFragment(R.xml.picture_prefs) {

    private lateinit var screenSettings: ScreenSettings
    private lateinit var darkModeManager: DarkModeManager
    private lateinit var darkModePreferences: DarkModePreferences
    private lateinit var backlightControlPolicy: BacklightControlPolicy

    private var backlightPref: SeekBarPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val app = requireActivity().application as App
        screenSettings = app.screenSettings
        darkModeManager = app.darkModeManager
        darkModePreferences = app.darkModePreferences
        backlightControlPolicy = app.backlightControlPolicy

        backlightPref = findPreference<LongPressGlobalSeekBarPreference>(MtkGlobalKeys.PICTURE_BACKLIGHT)?.apply {
            onClick(darkModeManager::toggleMode)
            onLongClick(darkModeManager::toggleFullDarkMode)
            setOnPreferenceChangeListener { _, newValue ->
                if (darkModeManager.activeMode == DarkMode.OFF) {
                    darkModePreferences.dayBacklight = newValue as Int
                }
                true
            }
        }
        findPreference<Preference>(PreferencesKeys.TOGGLE_SCREEN_POWER)?.onClick(screenSettings::toggleScreenPower)
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
                    darkModeHintsFlow().collect { hintResId ->
                        backlightPref?.summary = getString(hintResId)
                    }
                }
                launch {
                    backlightControlPolicy.adjustmentAllowedFlow.collect { canAdjustBacklight ->
                        backlightPref?.isEnabled = canAdjustBacklight
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun darkModeHintsFlow(): Flow<Int> = darkModeManager.state
        .map { it.activeMode }
        .distinctUntilChanged()
        .transformLatest { mode ->
            while (true) {
                emit(mode.getHintStringRes())
                delay(HOW_TO_SWITCH_MODE_HINT_DURATION)
                emit(R.string.hold_to_toggle_extra_dimm_hint)
                delay(HOLD_TO_DIMMING_HINT_DURATION)
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

    private fun DarkMode.getHintStringRes(): Int = when (this) {
        DarkMode.OFF -> R.string.click_to_dark_mode
        DarkMode.ONLY_BACKLIGHT -> if (darkModePreferences.isThreeStepsDarkModeEnabled) {
            R.string.click_to_turn_on_the_dark_filter
        } else {
            R.string.click_to_day_mode
        }
        DarkMode.FULL -> R.string.click_to_day_mode
    }

    companion object {
        private const val HOW_TO_SWITCH_MODE_HINT_DURATION = 7000L
        private const val HOLD_TO_DIMMING_HINT_DURATION = 3500L
    }
}