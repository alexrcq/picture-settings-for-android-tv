package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commitNow
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.service.AutoBacklightService
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.DAY_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_AUTO_BACKLIGHT_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.IS_DARK_FILTER_ENABLED
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NIGHT_TIME
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.PICTURE_MODE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.POWER_PICTURE_OFF
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.RESET_TO_DEFAULT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TEMPERATURE
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.appPreferences
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.LoadingDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.NotSupportedTVDialog
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.ResetToDefaultDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class PicturePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

    private lateinit var pictureSettings: PictureSettings
    private lateinit var autoBacklightService: AutoBacklightService
    private lateinit var appPreferences: AppPreferences

    private var backlightPref: SeekBarPreference? = null
    private var pictureModePref: ListPreference? = null
    private var takeScreenshotPref: Preference? = null
    private var temperaturePref: ListPreference? = null
    private var isDarkFilterEnabledPref: SwitchPreference? = null
    private var globalSettingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            val key = uri?.lastPathSegment
            if (key != null) {
                onGlobalSettingChanged(key)
            }
        }
    }

    private val onDarkManagerConnectedBR = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DarkModeManager.ACTION_SERVICE_CONNECTED) {
                Timber.d("loading completed")
                val loadingDialog =
                    childFragmentManager.findFragmentByTag(LoadingDialog.TAG) as DialogFragment?
                loadingDialog?.dismiss()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Timber.d("onCreatePreferences")
        setPreferencesFromResource(R.xml.picture_prefs, rootKey)
        iniPreferences()
        pictureSettings = PictureSettings(requireContext())
        bindAutoBacklightService()
    }

    private fun iniPreferences() {
        appPreferences = requireContext().appPreferences
        backlightPref = findPreference<SeekBarPreference?>(BACKLIGHT)?.apply {
            onPreferenceChangeListener = this@PicturePreferenceFragment
            setOnPreferenceClickListener {
                DarkModeManager.sharedInstance?.toggleDarkmode()
                updateBacklightPreferenceSummary()
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
        findPreference<Preference>(POWER_PICTURE_OFF)?.setOnPreferenceClickListener {
            pictureSettings.turnOffScreen()
            true
        }
        findPreference<Preference>(RESET_TO_DEFAULT)?.setOnPreferenceClickListener {
            ResetToDefaultDialog().show(childFragmentManager, ResetToDefaultDialog.TAG)
            true
        }
        findPreference<SeekBarPreference>(DARK_FILTER_POWER)?.max = 98
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    private var takeScreenshotJob: Job? = null

    private fun onTakeScreenshotClicked() {
        takeScreenshotJob?.cancel()
        takeScreenshotJob = viewLifecycleOwner.lifecycleScope.launch {
            val windowView = requireActivity().window.decorView
            windowView.isVisible = false
            with(AdbShell.getInstance(requireContext())) {
                connect()
                takeScreenshot()
            }
            windowView.isVisible = true
            takeScreenshotPref?.summary = getString(R.string.screenshot_saved)
            delay(3500)
            takeScreenshotPref?.summary = ""
        }
    }

    private fun updateBacklightPreferenceSummary() {
        backlightPref?.summary = if (appPreferences.isDarkModeEnabled)
            getString(R.string.click_to_day_mode)
        else
            getString(R.string.click_to_dark_mode)
    }

    private fun onGlobalSettingChanged(key: String) {
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
            BACKLIGHT -> {
                val backlight = newValue as Int
                pictureSettings.backlight = backlight
                with(appPreferences) {
                    if (!isDarkModeEnabled) {
                        dayBacklight = backlight
                    }
                }
            }
            PICTURE_MODE -> {
                val pictureMode = (newValue as String).toInt()
                pictureSettings.pictureMode = pictureMode
                if (pictureMode == PictureSettings.PICTURE_MODE_USER) {
                    showPictureEqualizer()
                }
            }
            TEMPERATURE -> {
                pictureSettings.temperature = (newValue as String).toInt()
            }
            IS_DARK_FILTER_ENABLED -> {
                if (appPreferences.isDarkModeEnabled) {
                    DarkModeManager.requireInstance().darkFilter.isEnabled = newValue as Boolean
                }
            }
            NIGHT_BACKLIGHT -> {
                with(appPreferences) {
                    if (isDarkModeEnabled) {
                        pictureSettings.backlight = nightBacklight
                    }
                }
            }
            DARK_FILTER_POWER -> {
                DarkModeManager.requireInstance().darkFilter.alpha = (newValue as Int) / 100f
            }
            IS_AUTO_BACKLIGHT_ENABLED -> {
                autoBacklightService.handleAutoBacklight(isAutoBacklightEnabled = newValue as Boolean)
            }
            DAY_TIME -> {
                autoBacklightService.setDayModeTime(newValue as String)
            }
            NIGHT_TIME -> {
                autoBacklightService.setDarkModeTime(newValue as String)
            }
        }
        return true
    }

    private var isAutoBacklightServiceBound = false

    private val autoBacklightServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AutoBacklightService.ServiceBinder
            autoBacklightService = binder.getService()
            isAutoBacklightServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isAutoBacklightServiceBound = false
        }
    }

    private fun bindAutoBacklightService() {
        if (!isAutoBacklightServiceBound) {
            requireActivity().bindService(
                Intent(requireActivity(), AutoBacklightService::class.java),
                autoBacklightServiceConn,
                Context.BIND_AUTO_CREATE
            )
            isAutoBacklightServiceBound = true
        }
    }

    private fun unbindAutoBacklightService() {
        if (isAutoBacklightServiceBound) {
            requireActivity().unbindService(autoBacklightServiceConn)
            isAutoBacklightServiceBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        requireContext().registerReceiver(
            onDarkManagerConnectedBR,
            IntentFilter(DarkModeManager.ACTION_SERVICE_CONNECTED)
        )
        requireContext().contentResolver.registerContentObserver(
            Settings.Global.CONTENT_URI, true,
            globalSettingsObserver
        )
        if (DarkModeManager.sharedInstance == null) {
            Timber.d("DarkModeManager is loading...")
            LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
        }
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(onDarkManagerConnectedBR)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        AdbShell.getInstance(requireContext()).disconnect()
        unbindAutoBacklightService()
    }

    private fun updateUi() {
        try {
            backlightPref?.value = pictureSettings.backlight
            pictureModePref?.value = pictureSettings.pictureMode.toString()
            temperaturePref?.value = pictureSettings.temperature.toString()
        } catch (e: Settings.SettingNotFoundException) {
            NotSupportedTVDialog().show(childFragmentManager, NotSupportedTVDialog.TAG)
        }
        updateBacklightPreferenceSummary()
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
}