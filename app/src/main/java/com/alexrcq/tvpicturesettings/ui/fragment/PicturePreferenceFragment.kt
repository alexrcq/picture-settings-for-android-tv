package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.hasActiveTvSource
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.helper.GlobalSettingsObserver
import com.alexrcq.tvpicturesettings.helper.GlobalSettingsObserverImpl
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_DARK_FILTER_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TURN_OFF_SCREEN
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PicturePreferenceFragment : BasePreferenceFragment(R.xml.picture_prefs),
    GlobalSettingsObserver by GlobalSettingsObserverImpl(),
    GlobalSettingsObserver.OnGlobalSettingChangedCallback {

    @Inject
    lateinit var pictureSettings: PictureSettings

    @Inject
    lateinit var appPreferences: AppPreferences

    private var backlightPref: SeekBarPreference? = null
    private var takeScreenshotPref: Preference? = null
    private var isDarkFilterEnabledPref: SwitchPreference? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DarkModeManager.ACTION_SERVICE_CONNECTED) {
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    val loadingDialog =
                        childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?
                    loadingDialog?.dismiss()
                    Timber.d("loading completed")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniPreferences()
        requireContext().registerReceiver(
            broadcastReceiver,
            IntentFilter(DarkModeManager.ACTION_SERVICE_CONNECTED)
        )
        registerGlobalSettingsObserver(viewLifecycleOwner, requireContext().contentResolver, this)
    }

    private fun iniPreferences() {
        with(appPreferences) {
            if (dayBacklight !in 0..100) {
                dayBacklight = pictureSettings.backlight
            }
        }
        backlightPref = findPreference<SeekBarPreference?>(BACKLIGHT)?.apply {
            onPreferenceChangeListener = this@PicturePreferenceFragment
            setOnPreferenceClickListener {
                onBacklightPreferenceClicked()
                true
            }
        }
        takeScreenshotPref = findPreference<Preference?>(TAKE_SCREENSHOT)?.apply {
            setOnPreferenceClickListener {
                onTakeScreenshotClicked()
                true
            }
        }
        isDarkFilterEnabledPref = findPreference(IS_DARK_FILTER_ENABLED)
        findPreference<Preference>(TURN_OFF_SCREEN)?.setOnPreferenceClickListener {
            onTurnOffScreenClicked()
            true
        }
    }

    private var takeScreenshotJob: Job? = null

    private fun onTakeScreenshotClicked() {
        takeScreenshotJob?.cancel()
        takeScreenshotJob = viewLifecycleOwner.lifecycleScope.launch {
            val windowView = requireActivity().window.decorView
            windowView.isVisible = false
            val adbShell = AdbShell.getInstance(requireContext())
            adbShell.connect()
            val resultSummary = try {
                withTimeout(7500) {
                    adbShell.takeScreenshot(
                        Environment.getExternalStorageDirectory().path + "/Screenshots"
                    )
                }
                getString(R.string.screenshot_saved)
            } catch (e: TimeoutCancellationException) {
                getString(R.string.screen_capture_error_try_again)
            }
            windowView.isVisible = true
            takeScreenshotPref?.summary = resultSummary
            delay(3500)
            takeScreenshotPref?.summary = ""
        }
    }

    private fun onTurnOffScreenClicked() {
        pictureSettings.turnOffScreen()
    }

    private fun onBacklightPreferenceClicked() {
        DarkModeManager.requireInstance().toggleDarkmode()
        updateBacklightPreferenceSummary()
    }

    override fun onGlobalSettingChanged(key: String) {
        if (key == GlobalSettings.Keys.PICTURE_BACKLIGHT) {
            backlightPref?.value = pictureSettings.backlight
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            BACKLIGHT -> onBacklightPreferenceChange(newValue)
            IS_DARK_FILTER_ENABLED -> onDarkFilterPreferenceChange(newValue)
            NIGHT_BACKLIGHT -> onNightBacklightPreferenceChange(newValue)
            DARK_FILTER_POWER -> onDarkFilterPowerPreferenceChange(newValue)
        }
        return true
    }

    private fun onDarkFilterPowerPreferenceChange(newValue: Any) {
        DarkModeManager.requireInstance().darkFilter.alpha = (newValue as Int) / 100f
    }

    private fun onNightBacklightPreferenceChange(newValue: Any) {
        if (appPreferences.isDarkModeEnabled) {
            pictureSettings.backlight = newValue as Int
        }
    }

    private fun onDarkFilterPreferenceChange(newValue: Any) {
        if (appPreferences.isDarkModeEnabled) {
            DarkModeManager.requireInstance().darkFilter.isEnabled = newValue as Boolean
        }
    }

    private fun onBacklightPreferenceChange(newValue: Any) {
        val backlight = newValue as Int
        pictureSettings.backlight = backlight
        with(appPreferences) {
            if (!isDarkModeEnabled) {
                dayBacklight = backlight
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        if (DarkModeManager.sharedInstance == null) {
            LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
            Timber.d("DarkModeManager is loading...")
        }
        if (requireContext().hasActiveTvSource) {
            takeScreenshotPref?.isEnabled = false
        }
        backlightPref?.value = pictureSettings.backlight
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
        AdbShell.getInstance(requireContext()).disconnect()
    }
}