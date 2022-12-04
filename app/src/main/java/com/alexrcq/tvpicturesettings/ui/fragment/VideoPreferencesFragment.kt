package com.alexrcq.tvpicturesettings.ui.fragment

import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.helper.GlobalSettingsObserver
import com.alexrcq.tvpicturesettings.helper.GlobalSettingsObserverImpl
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.ADAPTIVE_LUMA_CONTROL
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.BRIGHTNESS
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.CONTRAST
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.HDR
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.HUE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.LOCAL_CONTRAST_CONTROL
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.NOISE_REDUCTION
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.PICTURE_MODE
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.SATURATION
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.SHARPNESS
import com.alexrcq.tvpicturesettings.storage.AppPreferences.Keys.TEMPERATURE
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.ui.fragment.dialog.ResetToDefaultDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VideoPreferencesFragment : BasePreferenceFragment(R.xml.video_prefs),
    GlobalSettingsObserver by GlobalSettingsObserverImpl(),
    GlobalSettingsObserver.OnGlobalSettingChangedCallback {

    private lateinit var pictureModePref: ListPreference
    private lateinit var brightnessPref: SeekBarPreference
    private lateinit var contrastPref: SeekBarPreference
    private lateinit var saturationPref: SeekBarPreference
    private lateinit var huePref: SeekBarPreference
    private lateinit var sharpnessPref: SeekBarPreference
    private lateinit var temperaturePref: ListPreference
    private lateinit var noiseReductionPref: ListPreference
    private lateinit var adaptiveLumaPref: SwitchPreference
    private lateinit var localContrastPref: SwitchPreference
    private lateinit var hdrPref: SwitchPreference

    @Inject
    lateinit var pictureSettings: PictureSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniPreferences()
        registerGlobalSettingsObserver(viewLifecycleOwner, requireContext().contentResolver, this)
    }

    private fun iniPreferences() {
        pictureModePref = requirePreference(PICTURE_MODE)
        brightnessPref = requirePreference(BRIGHTNESS)
        contrastPref = requirePreference(CONTRAST)
        saturationPref = requirePreference(SATURATION)
        huePref = requirePreference(HUE)
        sharpnessPref = requirePreference(SHARPNESS)
        noiseReductionPref = requirePreference(NOISE_REDUCTION)
        adaptiveLumaPref = requirePreference(ADAPTIVE_LUMA_CONTROL)
        localContrastPref = requirePreference(LOCAL_CONTRAST_CONTROL)
        hdrPref = requirePreference(HDR)
        temperaturePref = requirePreference(TEMPERATURE)
        findPreference<Preference>(AppPreferences.Keys.RESET_TO_DEFAULT)?.setOnPreferenceClickListener {
            onResetToDefaultClicked()
            true
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            PICTURE_MODE -> pictureSettings.pictureMode = (newValue as String).toInt()
            BRIGHTNESS -> {
                pictureSettings.brightness = newValue as Int
                setupUserMode()
            }
            CONTRAST -> {
                pictureSettings.contrast = newValue as Int
                setupUserMode()
            }
            SATURATION -> {
                pictureSettings.saturation = newValue as Int
                setupUserMode()
            }
            HUE -> {
                pictureSettings.hue = newValue as Int
                setupUserMode()
            }
            SHARPNESS -> {
                pictureSettings.sharpness = newValue as Int
                setupUserMode()
            }
            TEMPERATURE -> pictureSettings.temperature = (newValue as String).toInt()
            ADAPTIVE_LUMA_CONTROL -> pictureSettings.isAdaptiveLumaEnabled = newValue as Boolean
            LOCAL_CONTRAST_CONTROL -> pictureSettings.isLocalContrastEnabled = newValue as Boolean
            NOISE_REDUCTION -> pictureSettings.noiseReduction = (newValue as String).toInt()
            HDR -> pictureSettings.isHdrEnabled = newValue as Boolean
        }
        return true
    }

    private fun setupUserMode() {
        if (pictureSettings.pictureMode != PictureSettings.PICTURE_MODE_USER) {
            pictureSettings.pictureMode = PictureSettings.PICTURE_MODE_USER
        }
    }

    override fun onGlobalSettingChanged(key: String) {
        val value = Settings.Global.getInt(requireContext().contentResolver, key)
        when (key) {
            GlobalSettings.Keys.PICTURE_MODE -> pictureModePref.value = value.toString()
            GlobalSettings.Keys.PICTURE_TEMPERATURE -> temperaturePref.value = value.toString()
            GlobalSettings.Keys.PICTURE_SATURATION -> saturationPref.value = value
            GlobalSettings.Keys.PICTURE_SHARPNESS -> sharpnessPref.value = value
            GlobalSettings.Keys.PICTURE_CONTRAST -> contrastPref.value = value
            GlobalSettings.Keys.PICTURE_BRIGHTNESS -> brightnessPref.value = value
            GlobalSettings.Keys.PICTURE_HUE -> huePref.value = value
            GlobalSettings.Keys.PICTURE_LOCAL_CONTRAST -> localContrastPref.isChecked =
                pictureSettings.isLocalContrastEnabled
            GlobalSettings.Keys.PICTURE_ADAPTIVE_LUMA_CONTROL -> adaptiveLumaPref.isChecked =
                pictureSettings.isAdaptiveLumaEnabled
            GlobalSettings.Keys.PICTURE_LIST_HDR -> hdrPref.isChecked = pictureSettings.isHdrEnabled
        }
    }

    override fun onStart() {
        super.onStart()
        updateUi()
    }

    private fun updateUi() {
        pictureModePref.value = pictureSettings.pictureMode.toString()
        brightnessPref.value = pictureSettings.brightness
        contrastPref.value = pictureSettings.contrast
        saturationPref.value = pictureSettings.saturation
        huePref.value = pictureSettings.hue
        sharpnessPref.value = pictureSettings.sharpness
        temperaturePref.value = pictureSettings.temperature.toString()
        noiseReductionPref.value = pictureSettings.noiseReduction.toString()
        localContrastPref.isChecked = pictureSettings.isLocalContrastEnabled
        adaptiveLumaPref.isChecked = pictureSettings.isAdaptiveLumaEnabled
        hdrPref.isChecked = pictureSettings.isHdrEnabled
    }

    private fun onResetToDefaultClicked() {
        ResetToDefaultDialog().show(childFragmentManager, ResetToDefaultDialog.TAG)
    }
}