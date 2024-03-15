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
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.TvConstants
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.APP_DESCRIPTION
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.OPEN_PICTURE_SETTINGS
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.TOGGLE_SCREEN_POWER
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.VIDEO_PREFERENCES
import com.alexrcq.tvpicturesettings.ui.fragment.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.ui.fragment.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.preference.LongPressGlobalSeekbarPreference
import com.alexrcq.tvpicturesettings.util.DarkModeHintProvider
import com.alexrcq.tvpicturesettings.util.TvUtils
import com.alexrcq.tvpicturesettings.util.hasPermission
import com.alexrcq.tvpicturesettings.util.onClick
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
                darkModePreferences,
                DarkModeHintProvider(darkModePreferences),
                adbClient,
                CaptureScreenUseCase(adbClient),
                tvSettingsRepository
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPreferences()
        viewModel.uiStateFlow.onEach(::updateUiState).launchIn(viewLifecycleOwner.lifecycleScope)
        viewModel.sideEffectFlow.onEach(::handleSideEffect).launchIn(viewLifecycleOwner.lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.darkModeHints.collect { hintResId ->
                        backlightPref?.summary = getString(hintResId)
                    }
                }
                launch {
                    viewModel.isTvSourceInactiveFlow.collect { isTvSourceInactive ->
                        takeScreenshotPref?.isEnabled = isTvSourceInactive
                    }
                }
                launch {
                    viewModel.isBacklightAdjustAllowedFlow.collect { isBacklightAdjustAllowed ->
                        backlightPref?.isEnabled = isBacklightAdjustAllowed
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
            isVisible = !TvUtils.isModelMssp()
            onClick(::openAppropriatePictureSettings)
        }
        findPreference<Preference>(VIDEO_PREFERENCES)?.isVisible = TvUtils.isModelMssp()
        findPreference<Preference>(APP_DESCRIPTION)?.summary =
            getString(R.string.app_description, BuildConfig.VERSION_NAME)
    }

    override fun onStart() {
        super.onStart()
        requirePermissions(
            listOf(
                android.Manifest.permission.WRITE_SECURE_SETTINGS,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun updateUiState(state: MainUiState) {
        when (state) {
            is MainUiState.Idle -> {
                takeScreenshotPref?.summary = ""
                val loadingDialog = childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?
                loadingDialog?.dismiss()
            }
            is MainUiState.Loading -> {
                LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
            }
            is MainUiState.ScreenCapturing -> {
                setWindowVisible(false)
            }
            is MainUiState.ScreenCaptureFinished -> {
                setWindowVisible(true)
                takeScreenshotPref?.summary = if (state.isSuccess) {
                    getString(R.string.screenshot_saved)
                } else {
                    getString(R.string.screen_capture_error_try_again)
                }
            }
        }
    }

    private fun handleSideEffect(sideEffect: MainSideEffect) {
        when (sideEffect) {
            is MainSideEffect.ShowAdbRequired -> showAdbRequired()
            is MainSideEffect.ShowGrantPermissions -> showGrantPermissionsDialog(sideEffect.permissions)
            is MainSideEffect.ShowPermissionsGranted -> showToast(getString(R.string.permissions_granted))
            is MainSideEffect.ShowError -> showToast(sideEffect.errorMessage)
        }
    }

    private fun showAdbRequired() {
        val dialog = AlertDialog.Builder(context)
            .setMessage(R.string.adb_debugging_required)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
                requireActivity().finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> requireActivity().finish() }
            .setOnCancelListener { requireActivity().finish() }
            .create()
        dialog.show()
        dialog.getButton(BUTTON_POSITIVE).requestFocus()
    }

    private fun showGrantPermissionsDialog(missingPermissions: List<String>) {
        val dialog = AlertDialog.Builder(context)
            .setMessage(R.string.wait_for_debug_window)
            .setPositiveButton(getString(R.string.grant_permissions)) { dialog, _ ->
                viewModel.processIntent(MainIntent.GrantPermissions(missingPermissions))
                dialog.dismiss()
            }
            .setOnCancelListener { requireActivity().finish() }
            .create()
        dialog.show()
        dialog.getButton(Dialog.BUTTON_POSITIVE).requestFocus()
    }

    private fun openAppropriatePictureSettings() {
        if (TvUtils.isModel4kSmartTv()) {
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

    private fun requirePermissions(permissions: List<String>) {
        val missingPermissions = permissions.filter { !requireContext().hasPermission(it) }
        if (missingPermissions.isEmpty()) {
            return
        }
        if (!viewModel.isAdbEnabled) {
            showAdbRequired()
            return
        }
        showGrantPermissionsDialog(missingPermissions)
    }
}