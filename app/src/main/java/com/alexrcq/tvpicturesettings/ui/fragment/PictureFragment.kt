package com.alexrcq.tvpicturesettings.ui.fragment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.*
import com.alexrcq.tvpicturesettings.TvConstants.TV_MODEL_CROODS
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_DARK_FILTER_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.OPEN_PICTURE_SETTINGS
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.VIDEO_PREFERENCES
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.POWER_PICTURE_OFF
import com.alexrcq.tvpicturesettings.storage.appPreferences
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AdbRequiredDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File

private const val SCREENSHOTS_FOLDER_NAME = "Screenshots"
private const val SCREEN_CAPTURE_TIMEOUT = 7500L
private const val SCREEN_CAPTURE_MESSAGE_DURATION = 3500L

class PictureFragment : GlobalSettingsFragment(R.xml.picture_prefs) {

    private lateinit var appPreferences: AppPreferences
    private lateinit var adbShell: AdbShell

    private var backlightPref: SeekBarPreference? = null
    private var takeScreenshotPref: Preference? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DarkModeManager.ACTION_SERVICE_CONNECTED) {
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    val loadingDialog =
                        childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?
                    loadingDialog?.dismiss()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreferences = requireContext().appPreferences
        adbShell = AdbShell(requireContext())
        iniPreferences()
        requireContext().registerReceiver(
            broadcastReceiver, IntentFilter(DarkModeManager.ACTION_SERVICE_CONNECTED)
        )
    }

    private fun iniPreferences() {
        with(appPreferences) {
            if (dayBacklight !in 0..100) {
                dayBacklight = globalSettings.getInt(PICTURE_BACKLIGHT)
            }
        }
        backlightPref = findPreference<SeekBarPreference>(PICTURE_BACKLIGHT)?.apply {
            setOnPreferenceClickListener {
                onBacklightPreferenceClicked()
                true
            }
        }
        takeScreenshotPref = findPreference<Preference>(TAKE_SCREENSHOT)?.apply {
            setOnPreferenceClickListener {
                onTakeScreenshotClicked()
                true
            }
        }
        findPreference<Preference>(POWER_PICTURE_OFF)?.setOnPreferenceClickListener {
            globalSettings.putInt(POWER_PICTURE_OFF, 0)
            true
        }
        findPreference<Preference>(OPEN_PICTURE_SETTINGS)?.apply {
            isVisible = isModelName(TV_MODEL_CROODS)
            setOnPreferenceClickListener {
                openPictureSettings()
                true
            }
        }
        findPreference<Preference>(VIDEO_PREFERENCES)?.isVisible = !isModelName(TV_MODEL_CROODS)
    }

    private fun openPictureSettings() {
        val pictureSettingsIntent = Intent().apply {
            component = ComponentName(
                TvConstants.TV_SETTINGS_PACKAGE, TvConstants.TV_PICTURE_ACTIVITY_NAME
            )
        }
        startActivity(pictureSettingsIntent)
    }

    private var takeScreenshotJob: Job? = null

    private fun onTakeScreenshotClicked() {
        takeScreenshotJob?.cancel()
        if (!requireContext().isAdbEnabled) {
            AdbRequiredDialog().show(childFragmentManager, AdbRequiredDialog.TAG)
            return
        }
        val windowView = requireActivity().window.decorView
        windowView.isVisible = false
        takeScreenshotJob = viewLifecycleOwner.lifecycleScope.launch {
            withTimeout(SCREEN_CAPTURE_TIMEOUT) {
                adbShell.connect()
                adbShell.captureScreen(saveDir = prepareScreenshotsDir())
            }
        }
        takeScreenshotJob?.invokeOnCompletion { cause ->
            windowView.isVisible = true
            when (cause) {
                null -> showScreenCaptureResultMessage(R.string.screenshot_saved)
                else -> showScreenCaptureResultMessage(
                    R.string.screen_capture_error_try_again
                )
            }
        }
    }

    private suspend fun prepareScreenshotsDir(): File {
        if (!requireContext().hasPermission(READ_EXTERNAL_STORAGE)) {
            adbShell.grantPermission(READ_EXTERNAL_STORAGE)
        }
        if (!requireContext().hasPermission(WRITE_EXTERNAL_STORAGE)) {
            adbShell.grantPermission(WRITE_EXTERNAL_STORAGE)
        }
        val fullPath =
            Environment.getExternalStorageDirectory().path + "/" + SCREENSHOTS_FOLDER_NAME
        val screenshotsDir = File(fullPath)
        if (screenshotsDir.exists()) {
            return screenshotsDir
        }
        screenshotsDir.mkdirs()
        return screenshotsDir
    }

    private var delayJob: Job? = null

    private fun showScreenCaptureResultMessage(@StringRes message: Int) {
        delayJob?.cancel()
        takeScreenshotPref?.summary = getString(message)
        delayJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(SCREEN_CAPTURE_MESSAGE_DURATION)
        }
        delayJob?.invokeOnCompletion {
            takeScreenshotPref?.summary = ""
        }
    }

    private fun onBacklightPreferenceClicked() {
        DarkModeManager.requireInstance().toggleDarkmode()
        updateBacklightPreferenceSummary()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        when (preference.key) {
            PICTURE_BACKLIGHT -> onBacklightPreferenceChange(newValue)
            IS_DARK_FILTER_ENABLED -> onDarkFilterPreferenceChange(newValue)
            NIGHT_BACKLIGHT -> onNightBacklightPreferenceChange(newValue)
            DARK_FILTER_POWER -> DarkModeManager.requireInstance().darkFilter.alpha =
                (newValue as Int) / 100f
        }
        return true
    }

    private fun onNightBacklightPreferenceChange(newValue: Any) {
        if (DarkModeManager.requireInstance().isDarkModeEnabled) {
            globalSettings.putInt(PICTURE_BACKLIGHT, newValue as Int)
        }
    }

    private fun onDarkFilterPreferenceChange(newValue: Any) {
        with(DarkModeManager.requireInstance()) {
            if (isDarkModeEnabled) {
                darkFilter.isEnabled = newValue as Boolean
            }
        }
    }

    private fun onBacklightPreferenceChange(newValue: Any) {
        if (!DarkModeManager.requireInstance().isDarkModeEnabled) {
            appPreferences.dayBacklight = newValue as Int
        }
    }

    override fun onStart() {
        super.onStart()
        if (DarkModeManager.sharedInstance == null) {
            LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
        }
        if (requireContext().hasActiveTvSource) {
            takeScreenshotPref?.isEnabled = false
        }
        updateBacklightPreferenceSummary()
    }

    private fun updateBacklightPreferenceSummary() {
        backlightPref?.summary = if (appPreferences.isDarkModeEnabled) {
            getString(R.string.click_to_day_mode)
        } else {
            getString(R.string.click_to_dark_mode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(broadcastReceiver)
        adbShell.disconnect()
    }
}