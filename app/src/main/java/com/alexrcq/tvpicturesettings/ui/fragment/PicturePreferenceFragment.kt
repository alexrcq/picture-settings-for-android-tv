package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.commitNow
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.*
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsImpl
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.util.DialogButton.NEGATIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.makeButtonFocused


class PicturePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener,
    GlobalSettingsImpl.OnGlobalSettingChangedCallback {

    private var backlightPref: SeekBarPreference? = null
    private var pictureModePref: ListPreference? = null
    private var temperaturePref: ListPreference? = null
    private var isDarkFilterEnabledPref: SwitchPreference? = null
    private var darkFilterBroadcastReceiver: BroadcastReceiver? = null
    private lateinit var appPreferences: AppPreferences
    private lateinit var pictureSettings: PictureSettings


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.picture_prefs, rootKey)
        darkFilterBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                isDarkFilterEnabledPref?.isChecked = false
            }
        }
        appPreferences = AppPreferences.getInstance(requireContext())
        pictureSettings = PictureSettings.getInstance(requireContext())
        backlightPref = findPreference<SeekBarPreference?>(AppPreferences.Keys.BACKLIGHT)?.apply {
            onPreferenceChangeListener = this@PicturePreferenceFragment
            setOnPreferenceClickListener {
                toggleDarkmode()
            }
        }
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
        findPreference<SeekBarPreference>(AppPreferences.Keys.DARK_FILTER_POWER)?.max = 98
        preferenceScreen.forEach { preference ->
            preference.onPreferenceChangeListener = this
        }
    }

    private fun toggleDarkmode(): Boolean {
        with(appPreferences) {
            isDarkModeActivated = !isDarkModeActivated
        }
        return true
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            AppPreferences.Keys.BACKLIGHT -> {
                val backlight = newValue as Int
                pictureSettings.backlight = backlight
                with(appPreferences) {
                    if (!isDarkModeActivated) {
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
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == AppPreferences.Keys.IS_DARK_MODE_ACTIVATED) {
            showModeChangedMessage()
        }
    }

    private fun showModeChangedMessage() {
        val messageResId: Int = if (appPreferences.isDarkModeActivated) {
            R.string.dark_mode_activated
        } else {
            R.string.day_mode_activated
        }
        Toast.makeText(
            requireContext(),
            messageResId,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onStart() {
        super.onStart()
        pictureSettings.addOnSettingsChangedCallback(this)
        appPreferences.registerOnSharedPreferenceChangedListener(this)
        requireContext().registerReceiver(
            darkFilterBroadcastReceiver,
            IntentFilter(DarkFilterService.ACTION_DARK_FILTER_SERVICE_CONNECTED)
        )
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        pictureSettings.removeOnSettingsChangedCallback(this)
        appPreferences.unregisterOnSharedPreferenceChangedListener(this)
        requireContext().unregisterReceiver(darkFilterBroadcastReceiver)
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

    private fun showResetToDefaultDialog() {
        AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.reset_to_default_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                pictureSettings.resetToDefault()
                Toast.makeText(requireContext(), R.string.please_wait, Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                show()
                makeButtonFocused(NEGATIVE_BUTTON)
            }
    }

    override fun onGlobalSettingChanged(key: String, value: Int) {
        when (key) {
            PictureSettings.KEY_PICTURE_BACKLIGHT -> backlightPref?.value = value
            PictureSettings.KEY_PICTURE_MODE -> pictureModePref?.value = value.toString()
            PictureSettings.KEY_PICTURE_TEMPERATURE -> temperaturePref?.value = value.toString()
        }
    }
}