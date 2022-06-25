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
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.util.DialogButton.NEGATIVE_BUTTON
import com.alexrcq.tvpicturesettings.util.makeButtonFocused


class PicturePreferenceFragment : LeanbackPreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener {

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

    private val sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                AppPreferences.Keys.IS_DARK_MODE_ACTIVATED -> {
                    if (appPreferences.isDarkModeActivated) {
                        Toast.makeText(
                            requireContext(),
                            "Активирован ночной режим",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        return@OnSharedPreferenceChangeListener
                    }
                    Toast.makeText(
                        requireContext(),
                        "Активирован дневной режим",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

    private val onSettingsChangedCallback = { key: String, value: Int ->
        when (key) {
            PictureSettings.KEY_PICTURE_BACKLIGHT -> backlightPref?.value = value
            PictureSettings.KEY_PICTURE_MODE -> pictureModePref?.value = value.toString()
            PictureSettings.KEY_PICTURE_TEMPERATURE -> temperaturePref?.value = value.toString()
        }
    }

    override fun onStart() {
        super.onStart()
        pictureSettings.addOnSettingsChangedCallback(onSettingsChangedCallback)
        appPreferences.registerOnSharedPreferenceChangedListener(sharedPreferenceChangeListener)
        requireContext().registerReceiver(
            darkFilterBroadcastReceiver,
            IntentFilter(DarkFilterService.ACTION_DARK_FILTER_SERVICE_INSTANTIATED)
        )
        updateUi()
    }

    override fun onStop() {
        super.onStop()
        pictureSettings.removeOnSettingsChangedCallback(onSettingsChangedCallback)
        appPreferences.unregisterOnSharedPreferenceChangedListener(sharedPreferenceChangeListener)
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
}