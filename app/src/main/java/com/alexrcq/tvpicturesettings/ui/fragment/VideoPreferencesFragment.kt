package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.RESET_TO_DEFAULT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_ADAPTIVE_LUMA_CONTROL
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_BRIGHTNESS
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_CONTRAST
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_HUE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_LIST_HDR
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_LOCAL_CONTRAST
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_MODE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_SATURATION
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_SHARPNESS
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Keys.PICTURE_TEMPERATURE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_MODE_BRIGHT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_MODE_DEFAULT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_MODE_MOVIE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_MODE_SPORT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_MODE_USER
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_TEMPERATURE_COLD
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_TEMPERATURE_DEFAULT
import com.alexrcq.tvpicturesettings.storage.GlobalSettings.Values.PICTURE_TEMPERATURE_WARM
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.ResetToDefaultDialog

class VideoPreferencesFragment : GlobalSettingsFragment(R.xml.video_prefs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>(RESET_TO_DEFAULT)?.setOnPreferenceClickListener {
            ResetToDefaultDialog().show(childFragmentManager, ResetToDefaultDialog.TAG)
            true
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        when (preference.key) {
            PICTURE_MODE -> setPictureTemperature(pictureMode = (newValue as String).toInt())
            PICTURE_BRIGHTNESS, PICTURE_CONTRAST, PICTURE_SATURATION, PICTURE_HUE, PICTURE_SHARPNESS -> {
                globalSettings.putInt(PICTURE_MODE, PICTURE_MODE_USER)
            }
            PICTURE_LOCAL_CONTRAST -> {
                val enabled = newValue as Boolean
                val isLocalContrastEnabled = if (enabled) 0 else 2
                globalSettings.putInt(preference.key, isLocalContrastEnabled)
            }
            PICTURE_ADAPTIVE_LUMA_CONTROL -> {
                val enabled = newValue as Boolean
                val isAdaptiveLumaEnabled = if (enabled) 0 else 2
                globalSettings.putInt(preference.key, isAdaptiveLumaEnabled)
            }
        }
        return true
    }

    private fun setPictureTemperature(pictureMode: Int) {
        val temperature: Int? = when (pictureMode) {
            PICTURE_MODE_DEFAULT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_BRIGHT -> PICTURE_TEMPERATURE_WARM
            PICTURE_MODE_SPORT -> PICTURE_TEMPERATURE_DEFAULT
            PICTURE_MODE_MOVIE -> PICTURE_TEMPERATURE_COLD
            else -> null
        }
        if (temperature != null) {
            globalSettings.putInt(PICTURE_TEMPERATURE, temperature)
        }
    }

    override fun updatePreference(preference: Preference) {
        super.updatePreference(preference)
        when (preference.key) {
            PICTURE_LOCAL_CONTRAST -> {
                val localContrastPref = preference as SwitchPreference
                localContrastPref.isChecked = when (globalSettings.getInt(PICTURE_LOCAL_CONTRAST)) {
                    0 -> true
                    else -> false
                }
            }
            PICTURE_ADAPTIVE_LUMA_CONTROL -> {
                val adaptiveLumaPref = preference as SwitchPreference
                adaptiveLumaPref.isChecked =
                    when (globalSettings.getInt(PICTURE_ADAPTIVE_LUMA_CONTROL)) {
                        0 -> true
                        else -> false
                    }
            }
            PICTURE_LIST_HDR -> {
                val hdrPref = preference as SwitchPreference
                hdrPref.isChecked = when (globalSettings.getInt(PICTURE_LIST_HDR)) {
                    1 -> true
                    else -> false
                }
            }
        }
    }
}