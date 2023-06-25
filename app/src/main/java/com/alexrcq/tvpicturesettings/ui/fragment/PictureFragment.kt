package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.*
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.helper.AppSettings
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.helper.GlobalSettings
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_BACKLIGHT
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AdbRequiredDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.preference.LongPressGlobalSeekbarPreference
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PictureFragment : GlobalSettingsFragment(R.xml.picture_prefs) {

    private lateinit var backlightPref: SeekBarPreference
    private lateinit var takeScreenshotPref: Preference

    private val viewModel: PictureViewModel by viewModels()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DarkFilterService.ACTION_SERVICE_CONNECTED) {
                viewModel.processIntent(MenuIntent.ChangeMenuState(MenuState.Idle))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniPreferences()
        requireContext().registerReceiver(
            broadcastReceiver, IntentFilter(DarkFilterService.ACTION_SERVICE_CONNECTED)
        )
        viewModel.menuState.onEach(::render).launchIn(lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isDarkModeEnabledFlow.collect { isDarkModeEnabled ->
                backlightPref.summary = if (isDarkModeEnabled) {
                    getString(R.string.click_to_day_mode)
                } else {
                    getString(R.string.click_to_dark_mode)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.darkModeHintFlow.collect { hintResId ->
                backlightPref.summary = getString(hintResId)
            }
        }
    }

    private fun iniPreferences() {
        backlightPref =
            requirePreference<LongPressGlobalSeekbarPreference>(PICTURE_BACKLIGHT).apply {
                setOnPreferenceClickListener {
                    appSettings.toggleDarkMode()
                    true
                }
                onPreferenceLongClickListener = {
                    onBacklightPreferenceLongClick()
                    true
                }
            }
        takeScreenshotPref = requirePreference<Preference>(TAKE_SCREENSHOT).apply {
            setOnPreferenceClickListener {
                if (!requireContext().isAdbEnabled) {
                    AdbRequiredDialog().show(childFragmentManager, AdbRequiredDialog.TAG)
                    return@setOnPreferenceClickListener true
                }
                viewModel.processIntent(MenuIntent.CaptureScreenshot)
                true
            }
        }
        findPreference<Preference>(GlobalSettings.Keys.POWER_PICTURE_OFF)?.setOnPreferenceClickListener {
            globalSettings.putInt(GlobalSettings.Keys.POWER_PICTURE_OFF, 0)
            true
        }
        findPreference<Preference>(AppSettings.Keys.OPEN_PICTURE_SETTINGS)?.apply {
            isVisible = !Build.MODEL.contains(TvConstants.TV_MODEL_MSSP_PREFIX, true)
            setOnPreferenceClickListener {
                openPictureSettings()
                true
            }
        }
        findPreference<Preference>(AppSettings.Keys.VIDEO_PREFERENCES)?.isVisible =
            Build.MODEL.contains(TvConstants.TV_MODEL_MSSP_PREFIX, true)
        findPreference<Preference>(AppSettings.Keys.APP_DESCRIPTION)?.summary =
            getString(R.string.app_description, BuildConfig.VERSION_NAME)
    }

    override fun onStart() {
        super.onStart()
        takeScreenshotPref.isEnabled = !hasActiveTvSource(contentResolver)
    }

    private fun render(state: MenuState) {
        when (state) {
            is MenuState.Idle -> {
                takeScreenshotPref.summary = ""
                val loadingDialog =
                    childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?
                loadingDialog?.dismiss()
            }
            is MenuState.Loading -> {
                LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
            }
            is MenuState.ScreenCapturing -> {
                requireActivity().isWindowVisible = false
            }
            is MenuState.ScreenCaptureFinished -> {
                requireActivity().isWindowVisible = true
                takeScreenshotPref.summary = if (state.isSuccess) {
                    getString(R.string.screenshot_saved)
                } else {
                    getString(R.string.screen_capture_error_try_again)
                }
            }
        }
    }

    private fun onBacklightPreferenceLongClick() {
        if (DarkFilterService.sharedInstance == null) {
            viewModel.processIntent(MenuIntent.ChangeMenuState(MenuState.Loading))
        }
        with(appSettings) {
            isDarkModeEnabled = true
            toggleDarkFilter()
        }
        requireContext().showActivationToast(
            isActivated = appSettings.isDarkFilterEnabled,
            activationMessage = R.string.dark_filter_turning_on,
            deactivationMessage = R.string.dark_filter_turning_off
        )
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        if (preference.key == PICTURE_BACKLIGHT) {
            with(appSettings) {
                if (!isDarkModeEnabled) {
                    dayBacklight = newValue as Int
                }
            }
        }
        return true
    }

    private fun openPictureSettings() {
        val intent = Intent().apply {
            component = ComponentName(
                TvConstants.TV_SETTINGS_PACKAGE, TvConstants.TV_PICTURE_ACTIVITY_NAME
            )
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(broadcastReceiver)
    }
}