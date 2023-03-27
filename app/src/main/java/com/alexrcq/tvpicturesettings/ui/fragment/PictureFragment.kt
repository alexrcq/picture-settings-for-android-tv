package com.alexrcq.tvpicturesettings.ui.fragment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import com.alexrcq.tvpicturesettings.*
import com.alexrcq.tvpicturesettings.TvConstants.TV_MODEL_PREFIX_MSSP
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.APP_DESCRIPTION
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.OPEN_PICTURE_SETTINGS
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.VIDEO_PREFERENCES
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.POWER_PICTURE_OFF
import com.alexrcq.tvpicturesettings.storage.appPreferences
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.AdbRequiredDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.preference.LongPressGlobalSeekbarPreference
import kotlinx.coroutines.*

private const val SCREENSHOTS_FOLDER_NAME = "Screenshots"
private const val SCREEN_CAPTURE_TIMEOUT = 7500L
private const val DARK_FILTER_HINT_INTERVAL = 7000L
private const val HINT_DURATION = 3500L

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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            while (true) {
                backlightPref?.summary = getString(R.string.hold_to_toggle_extra_dimm_hint)
                delay(HINT_DURATION)
                backlightPref?.summary = getRelevantDarkModeHint()
                delay(DARK_FILTER_HINT_INTERVAL)
            }
        }
    }

    private fun iniPreferences() {
        with(appPreferences) {
            if (dayBacklight !in 0..100) {
                dayBacklight = globalSettings.getInt(PICTURE_BACKLIGHT)
            }
        }
        backlightPref = findPreference<LongPressGlobalSeekbarPreference>(PICTURE_BACKLIGHT)?.apply {
            setOnPreferenceClickListener {
                onBacklightPreferenceClick()
                true
            }
            onPreferenceLongClickListener = {
                onBacklightPreferenceLongClick()
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
            isVisible = !Build.MODEL.contains(TV_MODEL_PREFIX_MSSP, true)
            setOnPreferenceClickListener {
                openPictureSettings()
                true
            }
        }
        findPreference<Preference>(VIDEO_PREFERENCES)?.isVisible =
            Build.MODEL.contains(TV_MODEL_PREFIX_MSSP, true)
        findPreference<Preference>(APP_DESCRIPTION)?.summary =
            getString(R.string.app_description, BuildConfig.VERSION_NAME)
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
        requireActivity().window.decorView.isVisible = false
        takeScreenshotJob = viewLifecycleOwner.lifecycleScope.launch {
            withTimeout(SCREEN_CAPTURE_TIMEOUT) {
                adbShell.connect()
                adbShell.grantPermission(READ_EXTERNAL_STORAGE)
                adbShell.grantPermission(WRITE_EXTERNAL_STORAGE)
                adbShell.captureScreen(
                    saveDir = FileUtils.prepareFolder(
                        "${Environment.getExternalStorageDirectory().path}/$SCREENSHOTS_FOLDER_NAME"
                    )
                )
            }
        }
        takeScreenshotJob?.invokeOnCompletion(::onScreenshotJobCompleted)
    }

    private fun onScreenshotJobCompleted(cause: Throwable?) {
        requireActivity().window.decorView.isVisible = true
        val resultMessage = when (cause) {
            null -> getString(R.string.screenshot_saved)
            else -> getString(R.string.screen_capture_error_try_again)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            takeScreenshotPref?.summary = resultMessage
            delay(HINT_DURATION)
            takeScreenshotPref?.summary = ""
        }
    }

    private fun onBacklightPreferenceClick() {
        DarkModeManager.requireInstance().toggleDarkmode()
        backlightPref?.summary = getRelevantDarkModeHint()
    }

    private fun onBacklightPreferenceLongClick() {
        with(DarkModeManager.requireInstance()) {
            if (!isDarkModeEnabled) {
                isDarkModeEnabled = true
                backlightPref?.summary = getRelevantDarkModeHint()
                if (darkFilter.isEnabled) return
            }
            darkFilter.toggle()
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        if (preference.key == PICTURE_BACKLIGHT) {
            onBacklightPreferenceChange(newValue)
        }
        return true
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
        backlightPref?.summary = getRelevantDarkModeHint()
        takeScreenshotPref?.summary = ""
    }

    private fun getRelevantDarkModeHint(): String {
        return if (appPreferences.isDarkModeEnabled) {
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