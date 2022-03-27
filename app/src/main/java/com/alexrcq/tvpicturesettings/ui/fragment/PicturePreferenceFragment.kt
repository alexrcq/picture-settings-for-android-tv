package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.View
import androidx.fragment.app.commitNow
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.*
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.AutoBacklightService
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.util.DialogButton.NEGATIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.GlobalSettingsObserver
import com.alexrcq.tvpicturesettings.util.OnGlobalSettingChangedCallback
import com.alexrcq.tvpicturesettings.util.Utils
import com.alexrcq.tvpicturesettings.util.makeButtonFocused


class PicturePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    OnGlobalSettingChangedCallback, SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceChangeListener {

    private var backlightPref: SeekBarPreference? = null
    private var pictureModePref: ListPreference? = null
    private var temperaturePref: ListPreference? = null
    private var isDarkFilterEnabledPref: SwitchPreference? = null
    private var globalSettingsObserver = GlobalSettingsObserver()

    private var autoBacklightService: AutoBacklightService? = null

    private lateinit var pictureSettings: PictureSettings

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.picture_prefs, rootKey)
        pictureSettings = PictureSettings(requireContext())
        backlightPref = findPreference(Keys.BACKLIGHT)
        pictureModePref = findPreference(Keys.PICTURE_MODE)
        temperaturePref = findPreference(Keys.TEMPERATURE)
        isDarkFilterEnabledPref = findPreference(Keys.IS_DARK_FILTER_ENABLED)
        findPreference<Preference>(Keys.RESET_TO_DEFAULT)?.setOnPreferenceClickListener {
            showResetToDefaultDialog()
            true
        }
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            Keys.BACKLIGHT -> {
                pictureSettings.backlight = newValue as Int
            }
            Keys.PICTURE_MODE -> {
                val pictureMode = (newValue as String).toInt()
                pictureSettings.pictureMode = pictureMode
                if (pictureMode == GlobalSettings.PICTURE_MODE_USER) {
                    showPictureEqualizer()
                }
            }
            Keys.TEMPERATURE -> {
                pictureSettings.temperature = (newValue as String).toInt()
            }
            Keys.IS_DARK_FILTER_ENABLED -> {
                val isDarkFilterEnabled = newValue as Boolean
                if (isDarkFilterEnabled && !Utils.isDarkFilterServiceEnabled(requireContext())) {
                    showEnableDarkFilterDialog()
                    isDarkFilterEnabledPref?.isChecked = false
                    return false
                }
            }
            Keys.DAY_TIME -> {
                autoBacklightService?.setDaytimeLaunchTime(newValue as String)
            }
            Keys.NIGHT_TIME -> {
                autoBacklightService?.setNighttimeLaunchTime(newValue as String)
            }
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Keys.IS_AUTO_BACKLIGHT_ENABLED -> {
                val isAutoBacklightEnabled =
                    sharedPreferences.getBoolean(Keys.IS_AUTO_BACKLIGHT_ENABLED, false)
                if (isAutoBacklightEnabled) {
                    autoBacklightService?.enable()
                } else {
                    autoBacklightService?.disable()
                }
            }
            Keys.IS_DARK_FILTER_ENABLED -> {
                val isDarkFilterEnabled =
                    sharedPreferences.getBoolean(Keys.IS_DARK_FILTER_ENABLED, false)
                if (isDarkFilterEnabled) {
                    autoBacklightService?.enableDarkFilter()
                } else {
                    autoBacklightService?.disableDarkFilter()
                }
            }
            Keys.NIGHT_BACKLIGHT -> {
                autoBacklightService?.setNightBacklight(
                    sharedPreferences.getInt(Keys.NIGHT_BACKLIGHT, 0)
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalSettingsObserver.observe(requireContext().contentResolver, this)
    }

    override fun onGlobalSettingChanged(key: String) {
        when (key) {
            GlobalSettings.KEY_PICTURE_BACKLIGHT -> {
                backlightPref?.value = pictureSettings.backlight
            }
            GlobalSettings.KEY_PICTURE_MODE -> {
                pictureModePref?.value = pictureSettings.pictureMode.toString()
            }
            GlobalSettings.KEY_PICTURE_TEMPERATURE -> {
                temperaturePref?.value = pictureSettings.temperature.toString()
            }
        }
    }

    private var isAutoBacklightServiceBound = false

    private val autoBacklightServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val serviceBinder = service as AutoBacklightService.ServiceBinder
            autoBacklightService = serviceBinder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
            .registerOnSharedPreferenceChangeListener(this)
        if (!isAutoBacklightServiceBound) {
            requireContext().bindService(
                Intent(requireContext(), AutoBacklightService::class.java),
                autoBacklightServiceConnection,
                Context.BIND_AUTO_CREATE
            )
            isAutoBacklightServiceBound = true
        }
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
            .unregisterOnSharedPreferenceChangeListener(this)
        if (isAutoBacklightServiceBound) {
            requireContext().unbindService(autoBacklightServiceConnection)
            isAutoBacklightServiceBound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        globalSettingsObserver.stopObserving()
    }

    private fun updateUi() {
        backlightPref?.value = pictureSettings.backlight
        pictureModePref?.value = pictureSettings.pictureMode.toString()
        temperaturePref?.value = pictureSettings.temperature.toString()
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

    private fun showEnableDarkFilterDialog() {
        val onOpenSettingsClickListener = DialogInterface.OnClickListener { _, _ ->
            openTvSettings()
        }
        val alertDialog =
            AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.should_enable_dark_filter_message)
                .setPositiveButton(getString(R.string.open_settings), onOpenSettingsClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        alertDialog.show()
        alertDialog.makeButtonFocused(POSITIVE_BUTTON)
    }

    private fun openTvSettings() {
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun showResetToDefaultDialog() {
        val onResetClickListener = DialogInterface.OnClickListener { _, _ ->
            pictureSettings.resetToDefault()
        }
        val alertDialog =
            AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.reset_to_default_message)
                .setPositiveButton(android.R.string.ok, onResetClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        alertDialog.show()
        alertDialog.makeButtonFocused(NEGATIVE_BUTTON)
    }

    private object Keys {
        const val IS_AUTO_BACKLIGHT_ENABLED = "auto_backlight"
        const val BACKLIGHT = "backlight"
        const val DAY_TIME = "ab_day_time"
        const val NIGHT_TIME = "ab_night_time"
        const val IS_DARK_FILTER_ENABLED = "is_dark_filter_enabled"
        const val NIGHT_BACKLIGHT = "ab_night_backlight"
        const val PICTURE_MODE = "picture_mode"
        const val TEMPERATURE = "temperature"
        const val RESET_TO_DEFAULT = "reset_to_default"
    }
}