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
import com.alexrcq.tvpicturesettings.adblib.AdbShell
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

    private val viewModel: PictureViewModel by viewModels {
        val adbShell = AdbShell(requireContext())
        PictureViewModel.Factory(
            adbShell,
            appSettings,
            globalSettings,
            CaptureScreenUseCase(adbShell)
        )
    }

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
        viewModel.menuState.onEach(::render).launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.menuSideEffect.onEach(::handleEffect).launchIn(viewLifecycleOwner.lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.darkModeHintFlow.collect { hintResId ->
                backlightPref.summary = getString(hintResId)
            }
        }
    }

    private fun iniPreferences() {
        backlightPref =
            requirePreference<LongPressGlobalSeekbarPreference>(PICTURE_BACKLIGHT).apply {
                onClick { viewModel.processIntent(MenuIntent.ToggleDarkMode) }
                onLongClick(::onBacklightPreferenceLongClick)
            }
        takeScreenshotPref = requirePreference<Preference>(TAKE_SCREENSHOT).apply {
            onClick { viewModel.processIntent(MenuIntent.CaptureScreenshot) }
        }
        findPreference<Preference>(GlobalSettings.Keys.POWER_PICTURE_OFF)?.apply {
            onClick { viewModel.processIntent(MenuIntent.TurnOffScreen) }
        }
        findPreference<Preference>(AppSettings.Keys.OPEN_PICTURE_SETTINGS)?.apply {
            isVisible = !Build.MODEL.contains(TvConstants.TV_MODEL_MSSP_PREFIX, true)
            onClick(::openPictureSettings)
        }
        findPreference<Preference>(AppSettings.Keys.VIDEO_PREFERENCES)?.isVisible =
            Build.MODEL.contains(TvConstants.TV_MODEL_MSSP_PREFIX, true)
        findPreference<Preference>(AppSettings.Keys.APP_DESCRIPTION)?.summary =
            getString(R.string.app_description, BuildConfig.VERSION_NAME)
    }

    private fun onBacklightPreferenceLongClick() {
        if (DarkFilterService.sharedInstance == null) {
            viewModel.processIntent(MenuIntent.ChangeMenuState(MenuState.Loading))
        }
        viewModel.processIntent(MenuIntent.EnableDarkModeAndToggleFilter)
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

    private fun handleEffect(effect: MenuSideEffect) {
        when (effect) {
            is MenuSideEffect.ShowAdbRequiredMessage -> {
                AdbRequiredDialog().show(childFragmentManager, AdbRequiredDialog.TAG)
            }
            is MenuSideEffect.ShowDarkFilterEnabledMessage -> {
                requireContext().showActivationToast(
                    isActivated = effect.isDarkFilterEnabled,
                    activationMessage = R.string.dark_filter_turning_on,
                    deactivationMessage = R.string.dark_filter_turning_off
                )
            }
            is MenuSideEffect.ShowDarkModeStateChanged -> {
                backlightPref.summary = if (effect.isDarkModeEnabled) {
                    getString(R.string.click_to_day_mode)
                } else {
                    getString(R.string.click_to_dark_mode)
                }
            }
        }
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