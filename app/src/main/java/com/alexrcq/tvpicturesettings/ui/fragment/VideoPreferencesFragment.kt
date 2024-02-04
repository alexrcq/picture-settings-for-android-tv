package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.SwitchPreference
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.MtkPictureSettings.Companion.PICTURE_MODE_USER
import com.alexrcq.tvpicturesettings.util.onClick
import com.alexrcq.tvpicturesettings.util.requestFocusForced
import com.alexrcq.tvpicturesettings.util.showToast

class VideoPreferencesFragment : GlobalSettingsFragment(R.xml.video_prefs) {

    private lateinit var pictureSettings: TvSettings.Picture

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pictureSettings = (requireActivity().application as App).tvSettings.picture
        findPreference<Preference>(PreferencesKeys.RESET_TO_DEFAULT)?.onClick {
            showResetToDefaultDialog()
        }
        val isWhiteBalanceFixed = (requireActivity().application as App).picturePreferences.isWhiteBalanceFixed
        findPreference<ListPreference>(MtkGlobalKeys.PICTURE_TEMPERATURE)?.apply {
            isEnabled = !isWhiteBalanceFixed
            summaryProvider = SummaryProvider<ListPreference> { preference ->
                if (isWhiteBalanceFixed) {
                    getString(R.string.fixed_see_wb_settings)
                } else if (preference.entry.isNullOrEmpty()) {
                    getString(R.string.custom_see_wb_settings)
                } else {
                    preference.entry
                }
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        super.onPreferenceChange(preference, newValue)
        when (preference.key) {
            MtkGlobalKeys.PICTURE_MODE -> {
                pictureSettings.setPictureTemperature(pictureMode = (newValue as String).toInt())
            }
            MtkGlobalKeys.PICTURE_BRIGHTNESS,
            MtkGlobalKeys.PICTURE_CONTRAST,
            MtkGlobalKeys.PICTURE_SATURATION,
            MtkGlobalKeys.PICTURE_HUE,
            MtkGlobalKeys.PICTURE_SHARPNESS -> {
                pictureSettings.pictureMode = PICTURE_MODE_USER
            }
            MtkGlobalKeys.PICTURE_LOCAL_CONTRAST -> {
                pictureSettings.isLocalContrastEnabled = newValue as Boolean
            }
            MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL -> {
                pictureSettings.isAdaptiveLumaEnabled = newValue as Boolean
            }
            MtkGlobalKeys.PICTURE_LIST_HDR -> {
                pictureSettings.isHdrEnabled = newValue as Boolean
            }
        }
        return true
    }

    override fun updatePreference(preference: Preference) {
        super.updatePreference(preference)
        when (preference.key) {
            MtkGlobalKeys.PICTURE_LOCAL_CONTRAST -> {
                val localContrastPref = preference as SwitchPreference
                localContrastPref.isChecked = pictureSettings.isLocalContrastEnabled
            }
            MtkGlobalKeys.PICTURE_ADAPTIVE_LUMA_CONTROL -> {
                val adaptiveLumaPref = preference as SwitchPreference
                adaptiveLumaPref.isChecked = pictureSettings.isAdaptiveLumaEnabled
            }
            MtkGlobalKeys.PICTURE_LIST_HDR -> {
                val hdrPref = preference as SwitchPreference
                hdrPref.isChecked = pictureSettings.isHdrEnabled
            }
        }
    }

    private fun showResetToDefaultDialog() {
        AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Dialog_Alert)
            .setMessage(R.string.reset_to_default_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                showToast(getString(R.string.please_wait))
                pictureSettings.resetToDefault()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().apply {
                setOnShowListener { getButton(Dialog.BUTTON_NEGATIVE).requestFocusForced() }
            }.show()
    }
}