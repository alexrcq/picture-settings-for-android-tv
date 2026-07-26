package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.platform.Device
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.ui.fragment.base.BasePreferenceFragment
import com.alexrcq.tvpicturesettings.ui.viewmodel.ScreenCaptureSideEffect
import com.alexrcq.tvpicturesettings.ui.viewmodel.ScreenCaptureState
import com.alexrcq.tvpicturesettings.ui.viewmodel.ScreenCaptureViewModel
import com.alexrcq.tvpicturesettings.util.onClick
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.launch

class ScreenControlFragment : BasePreferenceFragment(R.xml.screen_control_prefs) {
    private var takeScreenshotPref: Preference? = null

    private val viewModel: ScreenCaptureViewModel by viewModels {
        with(requireActivity().application as App) {
            ScreenCaptureViewModel.provideFactory(adbShell, developerSettings, tvInputMonitor)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        takeScreenshotPref = findPreference<Preference>(TAKE_SCREENSHOT)?.apply {
            onClick { viewModel.captureScreen() }
        }
        if (!Device.hasInactivityDetection()) {
            findPreference<Preference>(PreferencesKeys.SCREEN_OFF_TIMEOUT)?.apply {
                isEnabled = false
                summaryProvider = Preference.SummaryProvider<Preference> {
                    getString(R.string.device_not_supported)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::renderScreenCaptureState) }
                launch { viewModel.sideEffects.collect(::handleScreenCaptureSideEffect) }
                launch {
                    viewModel.isScreenCaptureAvailable.collect {
                        takeScreenshotPref?.isEnabled = it
                    }
                }
            }
        }
    }

    private fun renderScreenCaptureState(state: ScreenCaptureState) {
        when (state) {
            ScreenCaptureState.Idle -> takeScreenshotPref?.summary = ""

            ScreenCaptureState.Capturing -> setWindowVisible(false)

            is ScreenCaptureState.Completed -> {
                setWindowVisible(true)
                takeScreenshotPref?.summary = if (state.success) {
                    getString(R.string.screenshot_saved)
                } else {
                    getString(R.string.screen_capture_error_try_again)
                }
            }
        }
    }

    private fun handleScreenCaptureSideEffect(sideEffect: ScreenCaptureSideEffect) {
        when (sideEffect) {
            is ScreenCaptureSideEffect.AdbRequired -> showAdbRequired()
            is ScreenCaptureSideEffect.Error -> showToast(sideEffect.message)
        }
    }

    private fun setWindowVisible(visible: Boolean) {
        requireActivity().window.decorView.isVisible = visible
    }

    private fun showAdbRequired() {
        val dialog = AlertDialog.Builder(context)
            .setMessage(R.string.adb_debugging_required)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_SETTINGS))
                requireActivity().finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setOnCancelListener { requireActivity().finish() }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus()
    }
}