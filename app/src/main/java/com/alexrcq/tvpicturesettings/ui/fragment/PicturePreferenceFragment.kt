package com.alexrcq.tvpicturesettings.ui.fragment

import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.BACKLIGHT
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.DARK_FILTER_POWER
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.DARK_MODE_TIME
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.DAY_MODE_TIME
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.IS_AUTO_BACKLIGHT_ENABLED
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.IS_DARK_FILTER_ENABLED
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.PICTURE_MODE
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.POWER_PICTURE_OFF
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.RESET_TO_DEFAULT
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.TAKE_SCREENSHOT
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment.PreferencesKeys.TEMPERATURE
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
        val darkModePrefs = DarkModeManager.Preferences(requireContext())
        backlightPref?.summary = if (darkModePrefs.isDarkModeEnabled)
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
                with(DarkModeManager.requireInstance()) {
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
                DarkModeManager.requireInstance().isDarkFilterOnDarkModeEnabled =
                    newValue as Boolean
            }
            NIGHT_BACKLIGHT -> {
                DarkModeManager.requireInstance().nightBacklight = newValue as Int
            }
            DARK_FILTER_POWER -> {
                DarkModeManager.requireInstance().darkFilterAlpha = (newValue as Int) / 100f
            }
            IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED -> {
                DarkModeManager.requireInstance().isDayModeAfterScreenOnEnabled =
                    newValue as Boolean
            }
            IS_AUTO_BACKLIGHT_ENABLED -> {
                autoBacklightService.isAutoBacklightEnabled = newValue as Boolean
            }
            DAY_MODE_TIME -> {
                autoBacklightService.dayModeTime = newValue as String
            }
            DARK_MODE_TIME -> {
                autoBacklightService.darkModeTime = newValue as String
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
        with(requireContext()) {
            registerReceiver(
                onDarkManagerConnectedBR,
                IntentFilter(DarkModeManager.ACTION_SERVICE_CONNECTED)
            )
            contentResolver.registerContentObserver(
                Settings.Global.CONTENT_URI, true,
                globalSettingsObserver
            )
        }
        if (DarkModeManager.sharedInstance == null) {
            Timber.d("DarkModeManager is loading...")
            LoadingDialog().show(childFragmentManager, LoadingDialog.TAG)
        }
        updateUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        Timber.d("onDetach")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("onDestroyView")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
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

    private object PreferencesKeys {
        const val IS_AUTO_BACKLIGHT_ENABLED = "auto_backlight"
        const val BACKLIGHT = "backlight"
        const val DAY_MODE_TIME = "ab_day_time"
        const val DARK_MODE_TIME = "ab_night_time"
        const val PICTURE_MODE = "picture_mode"
        const val TEMPERATURE = "temperature"
        const val RESET_TO_DEFAULT = "reset_to_default"
        const val POWER_PICTURE_OFF = "power_picture_off"
        const val TAKE_SCREENSHOT = "take_screenshot"

        const val IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED =
            "is_enable_day_mode_after_screen_on_enabled"
        const val IS_DARK_FILTER_ENABLED = "is_dark_filter_enabled"
        const val NIGHT_BACKLIGHT = "ab_night_backlight"
        const val DARK_FILTER_POWER = "dark_filter_power"
    }
}