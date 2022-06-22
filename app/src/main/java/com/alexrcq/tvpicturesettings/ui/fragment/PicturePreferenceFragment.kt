package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.commitNow
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.*
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.AutoBacklightManager
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.util.DialogButton.NEGATIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.DialogButton.POSITIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.Utils
import com.alexrcq.tvpicturesettings.util.makeButtonFocused


class PicturePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceChangeListener {

    private var backlightPref: SeekBarPreference? = null
    private var pictureModePref: ListPreference? = null
    private var temperaturePref: ListPreference? = null
    private var isDarkFilterEnabledPref: SwitchPreference? = null

    private lateinit var autoBacklightManager: AutoBacklightManager
    private lateinit var appPreferences: AppPreferences
    private lateinit var pictureSettings: PictureSettings

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateBacklightBarWithDelay()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.picture_prefs, rootKey)
        appPreferences = AppPreferences(requireContext())
        autoBacklightManager = AutoBacklightManager(requireContext())
        pictureSettings = PictureSettings.getInstance(requireContext())
        backlightPref = findPreference(AppPreferences.Keys.BACKLIGHT)
        pictureModePref = findPreference(AppPreferences.Keys.PICTURE_MODE)
        temperaturePref = findPreference(AppPreferences.Keys.TEMPERATURE)
        isDarkFilterEnabledPref = findPreference(AppPreferences.Keys.IS_DARK_FILTER_ENABLED)
        findPreference<Preference>(AppPreferences.Keys.POWER_PICTURE_OFF)?.setOnPreferenceClickListener {
            pictureSettings.turnOffScreen()
            true
        }
        findPreference<Preference>(AppPreferences.Keys.RESET_TO_DEFAULT)?.setOnPreferenceClickListener {
            showResetToDefaultDialog()
            true
        }
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            AppPreferences.Keys.BACKLIGHT -> {
                val backlight = newValue as Int
                pictureSettings.backlight = backlight
                with(appPreferences) {
                    if (isAutoBacklightEnabled && !isNightNow) {
                        dayBacklight = backlight
                    }
                }
            }
            AppPreferences.Keys.PICTURE_MODE -> {
                val pictureMode = (newValue as String).toInt()
                pictureSettings.pictureMode = pictureMode
                if (pictureMode == PictureSettings.PICTURE_MODE_USER) {
                    showPictureEqualizer()
                }
            }
            AppPreferences.Keys.TEMPERATURE -> {
                pictureSettings.temperature = (newValue as String).toInt()
            }
            AppPreferences.Keys.IS_DARK_FILTER_ENABLED -> {
                val isDarkFilterEnabled = newValue as Boolean
                if (isDarkFilterEnabled && !Utils.isDarkFilterServiceEnabled(requireContext())) {
                    showEnableDarkFilterDialog()
                    isDarkFilterEnabledPref?.isChecked = false
                    return false
                }
            }
            AppPreferences.Keys.DAY_TIME -> {
                autoBacklightManager.setDaytimeManagerLaunchTime(newValue as String)
                updateBacklightBarWithDelay()
            }
            AppPreferences.Keys.NIGHT_TIME -> {
                autoBacklightManager.setNighttimeLaunchTime(newValue as String)
                updateBacklightBarWithDelay()
            }
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            AppPreferences.Keys.IS_AUTO_BACKLIGHT_ENABLED -> {
                autoBacklightManager.switchAutoBacklight(enabled = appPreferences.isAutoBacklightEnabled)
                updateBacklightBarWithDelay()
            }
            AppPreferences.Keys.IS_DARK_FILTER_ENABLED -> {
                autoBacklightManager.switchDarkFilter(enabled = (appPreferences.isDarkFilterEnabled && appPreferences.isNightNow))
            }
            AppPreferences.Keys.NIGHT_BACKLIGHT -> {
                with(appPreferences) {
                    if (isNightNow) {
                        pictureSettings.backlight = nightBacklight
                        updateBacklightBarWithDelay()
                    }
                }
            }
        }
    }

    private val onSettingsChangedCallback = { key: String, value: Int ->
        when(key) {
            PictureSettings.KEY_PICTURE_MODE -> pictureModePref?.value = value.toString()
            PictureSettings.KEY_PICTURE_TEMPERATURE -> temperaturePref?.value = value.toString()
        }
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
            .registerOnSharedPreferenceChangeListener(this)
        requireActivity().registerReceiver(receiver, IntentFilter(ACTION_UPDATE_BACKLIGHT_BAR))
        pictureSettings.addOnSettingsChangedCallback(onSettingsChangedCallback)
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
            .unregisterOnSharedPreferenceChangeListener(this)
        pictureSettings.removeOnSettingsChangedCallback(onSettingsChangedCallback)
        requireActivity().unregisterReceiver(receiver)
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

    private fun updateBacklightBarWithDelay() {
        //wait for the shell
        Handler(Looper.getMainLooper()).postDelayed({
            backlightPref?.value = pictureSettings.backlight
        }, 200)
    }

    companion object {
        const val ACTION_UPDATE_BACKLIGHT_BAR = "com.alexrcq.tvpicturesettings.ui.fragment.ACTION_UPDATE_BACKLIGHT_BAR"
    }
}