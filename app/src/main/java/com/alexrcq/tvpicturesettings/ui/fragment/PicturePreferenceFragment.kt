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
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_MODE_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DAY_MODE_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_AUTO_DARK_MODE_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_DARK_FILTER_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.PICTURE_MODE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.RESET_TO_DEFAULT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TEMPERATURE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TURN_OFF_SCREEN
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.appPreferences
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.ResetToDefaultDialog
import com.alexrcq.tvpicturesettings.util.GlobalSettingsObserver
import com.alexrcq.tvpicturesettings.util.GlobalSettingsObserverImpl
import com.alexrcq.tvpicturesettings.util.hasActiveTvSource
import kotlinx.coroutines.*
import timber.log.Timber

class PicturePreferenceFragment : BasePreferenceFragment(R.xml.picture_prefs),
    GlobalSettingsObserver by GlobalSettingsObserverImpl(),
    GlobalSettingsObserver.OnGlobalSettingChangedCallback {

    private lateinit var pictureSettings: PictureSettings
    private lateinit var appPreferences: AppPreferences

    private var backlightPref: SeekBarPreference? = null
    private var pictureModePref: ListPreference? = null
    private var takeScreenshotPref: Preference? = null
    private var temperaturePref: ListPreference? = null
    private var isDarkFilterEnabledPref: SwitchPreference? = null

    private val onDarkManagerConnectedBR = object : BroadcastReceiver() {
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
        pictureSettings = PictureSettings(requireContext())
        iniPreferences()
        requireContext().registerReceiver(
            onDarkManagerConnectedBR,
            IntentFilter(DarkModeManager.ACTION_SERVICE_CONNECTED)
        )
        registerGlobalSettingsObserver(viewLifecycleOwner, requireContext().contentResolver, this)
    }

    private fun iniPreferences() {
        appPreferences = requireContext().appPreferences
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
        pictureModePref = findPreference(PICTURE_MODE)
        temperaturePref = findPreference(TEMPERATURE)
        isDarkFilterEnabledPref = findPreference(IS_DARK_FILTER_ENABLED)
        findPreference<Preference>(TURN_OFF_SCREEN)?.setOnPreferenceClickListener {
            onTurnOffScreenClicked()
            true
        }
        findPreference<Preference>(RESET_TO_DEFAULT)?.setOnPreferenceClickListener {
            onResetToDefaultClicked()
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

    private fun onResetToDefaultClicked() {
        ResetToDefaultDialog().show(childFragmentManager, ResetToDefaultDialog.TAG)
    }

    override fun onGlobalSettingChanged(key: String) {
        when (key) {
            PictureSettings.KEY_PICTURE_BACKLIGHT -> {
                backlightPref?.value = pictureSettings.backlight
            }
            PictureSettings.KEY_PICTURE_TEMPERATURE -> {
                temperaturePref?.value = pictureSettings.temperature.toString()
            }
            PictureSettings.KEY_PICTURE_MODE -> {
                pictureModePref?.value = pictureSettings.pictureMode.toString()
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            BACKLIGHT -> onBacklightPreferenceChange(newValue)
            PICTURE_MODE -> onPictureModePreferenceChange(newValue)
            TEMPERATURE -> onTemperaturePreferenceChange(newValue)
            IS_DARK_FILTER_ENABLED -> onDarkFilterPreferenceChange(newValue)
            NIGHT_BACKLIGHT -> onNightBacklightPreferenceChange(newValue)
            DARK_FILTER_POWER -> onDarkFilterPowerPreferenceChange(newValue)
            IS_AUTO_DARK_MODE_ENABLED -> onAutoDarkModeEnabledPreferenceChange(newValue)
            DARK_MODE_TIME -> onDarkModeTimePreferenceChange(newValue)
            DAY_MODE_TIME -> onDayModeTimePreferenceChange(newValue)
        }
        return true
    }

    private fun onDayModeTimePreferenceChange(newValue: Any) {
        DarkModeManager.requireInstance().setDayModeTime(newValue as String)
    }

    private fun onDarkModeTimePreferenceChange(newValue: Any) {
        DarkModeManager.requireInstance().setDarkModeTime(newValue as String)
    }

    private fun onAutoDarkModeEnabledPreferenceChange(newValue: Any) {
        DarkModeManager.requireInstance().setAutoDarkModeEnabled(newValue as Boolean)
    }

    private fun onDarkFilterPowerPreferenceChange(newValue: Any) {
        DarkModeManager.requireInstance().darkFilter.alpha = (newValue as Int) / 100f
    }

    private fun onNightBacklightPreferenceChange(newValue: Any) {
        with(appPreferences) {
            if (isDarkModeEnabled) {
                pictureSettings.backlight = newValue as Int
            }
        }
    }

    private fun onDarkFilterPreferenceChange(newValue: Any) {
        if (appPreferences.isDarkModeEnabled) {
            DarkModeManager.requireInstance().darkFilter.isEnabled = newValue as Boolean
        }
    }

    private fun onPictureModePreferenceChange(newValue: Any) {
        val pictureMode = (newValue as String).toInt()
        pictureSettings.pictureMode = pictureMode
        if (pictureMode == PictureSettings.PICTURE_MODE_USER) {
            showPictureEqualizer()
        }
    }

    private fun onTemperaturePreferenceChange(newValue: Any) {
        pictureSettings.temperature = (newValue as String).toInt()
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

    private fun showPictureEqualizer() {
        parentFragmentManager.popBackStackImmediate()
        parentFragmentManager.commitNow {
            replace(
                androidx.leanback.preference.R.id.settings_preference_fragment_container,
                PictureEqualizerPreferenceFragment()
            )
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
        pictureModePref?.value = pictureSettings.pictureMode.toString()
        temperaturePref?.value = pictureSettings.temperature.toString()
        updateBacklightPreferenceSummary()
    }

    private fun updateBacklightPreferenceSummary() {
        backlightPref?.summary = if (appPreferences.isDarkModeEnabled) {
            getString(R.string.click_to_day_mode)
        } else {
            getString(R.string.click_to_dark_mode)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(onDarkManagerConnectedBR)
        AdbShell.getInstance(requireContext()).disconnect()
    }
}