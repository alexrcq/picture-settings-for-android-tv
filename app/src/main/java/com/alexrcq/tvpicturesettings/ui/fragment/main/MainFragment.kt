package com.alexrcq.tvpicturesettings.ui.fragment.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.BuildConfig
import com.alexrcq.tvpicturesettings.CaptureScreenUseCase
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.APP_DESCRIPTION
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.OPEN_PICTURE_SETTINGS
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.TOGGLE_SCREEN_POWER
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.VIDEO_PREFERENCES
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.ui.fragment.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.ui.fragment.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.preference.LongPressGlobalSeekbarPreference
import com.alexrcq.tvpicturesettings.util.DeviceUtils
import com.alexrcq.tvpicturesettings.util.hasPermission
import com.alexrcq.tvpicturesettings.util.onClick
import com.alexrcq.tvpicturesettings.util.requestFocusForced
import com.alexrcq.tvpicturesettings.util.setWindowVisible
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainFragment : GlobalSettingsFragment(R.xml.picture_prefs) {

    private var backlightPref: SeekBarPreference? = null
    private var takeScreenshotPref: Preference? = null

    private val viewModel: MainViewModel by viewModels {
        with(requireActivity().application as App) {
            MainViewModel.provideFactory(
                darkModeManager, darkModePreferences, adbClient, tvSettings, CaptureScreenUseCase(adbClient)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
        viewModel.mainState.onEach(::render).launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.mainSideEffect.onEach(::handleEffect).launchIn(viewLifecycleOwner.lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.darkModeHints.collect { hintResId ->
                        backlightPref?.summary = getString(hintResId)
                    }
                }
                launch {
                    viewModel.isTvSourceActiveFlow.collect { isTvSourceActive ->
                        takeScreenshotPref?.isEnabled = !isTvSourceActive
                    }
                }
            }
        }
    }

    private fun initPreferences() {
        backlightPref = findPreference<LongPressGlobalSeekbarPreference>(MtkGlobalKeys.PICTURE_BACKLIGHT)?.apply {
            onClick { viewModel.processIntent(MainIntent.ToggleDarkMode) }
            onLongClick { viewModel.processIntent(MainIntent.EnableDarkModeAndToggleFilter) }
            onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
                viewModel.processIntent(MainIntent.ChangeBacklight(newValue as Int))
                true
            }
        }
        takeScreenshotPref = findPreference<Preference>(TAKE_SCREENSHOT)?.apply {
            onClick { viewModel.processIntent(MainIntent.CaptureScreenshot) }
        }
        findPreference<Preference>(TOGGLE_SCREEN_POWER)?.onClick {
            viewModel.processIntent(MainIntent.ToggleScreenPower)
        }
        findPreference<Preference>(OPEN_PICTURE_SETTINGS)?.apply {
            isVisible = !DeviceUtils.isModelMssp()
            onClick(::openAppropriatePictureSettings)
        }
        findPreference<Preference>(VIDEO_PREFERENCES)?.isVisible = DeviceUtils.isModelMssp()
        findPreference<Preference>(APP_DESCRIPTION)?.summary =
            getString(R.string.app_description, BuildConfig.VERSION_NAME)
    }

    override fun onStart() {
        super.onStart()
        requireWriteSettingsPermission()
    }

    private fun render(state: MainState) {
        when (state) {
            is MainState.Idle -> {
                takeScreenshotPref?.summary = ""
                val loadingDialog = childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?
                loadingDialog?.dismiss()
            }
            is MainState.Loading -> {
                LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
            }
            is MainState.ScreenCapturing -> {
                setWindowVisible(false)
            }
            is MainState.ScreenCaptureFinished -> {
                setWindowVisible(true)
                takeScreenshotPref?.summary = if (state.isSuccess) {
                    getString(R.string.screenshot_saved)
                } else {
                    getString(R.string.screen_capture_error_try_again)
                }
            }
        }
    }

    private fun handleEffect(effect: MainSideEffect) {
        when (effect) {
            is MainSideEffect.ShowAdbRequired -> showAdbRequired()
            is MainSideEffect.ShowAcceptAdbForPermissions -> showAcceptAdbForPermissions()
            is MainSideEffect.ShowError -> showToast(effect.message)
        }
    }

    private fun showAdbRequired() {
        AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.adb_debugging_required)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
                requireActivity().finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> requireActivity().finish() }
            .setOnCancelListener { requireActivity().finish() }
            .create().apply {
                setOnShowListener { getButton(BUTTON_POSITIVE).requestFocusForced() }
            }.show()
    }

    private fun showAcceptAdbForPermissions() {
        AlertDialog.Builder(requireActivity(), android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.wait_for_debug_window)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.processIntent(MainIntent.GrantPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS))
            }
            .setOnCancelListener { requireActivity().finish() }
            .create().apply {
                setOnShowListener { getButton(Dialog.BUTTON_POSITIVE).requestFocusForced() }
            }.show()
    }


    private fun openAppropriatePictureSettings() {
        if (DeviceUtils.isModel4kSmartTv()) {
            openPQAQSettings()
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

    private fun openPQAQSettings() {
        try {
            startActivity(Intent(TvConstants.ACTION_PQAQ_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            showToast("No activity for the action '${TvConstants.ACTION_PQAQ_SETTINGS}'")
        }
    }

    private fun requireWriteSettingsPermission() {
        if (requireContext().hasPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)) return
        if (!viewModel.isAdbEnabled) {
            showAdbRequired()
            return
        }
        showAcceptAdbForPermissions()
    }
}