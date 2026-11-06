package com.alexrcq.tvpicturesettings.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.platform.VideoSettings
import com.alexrcq.tvpicturesettings.platform.mtk.MtkVideoSettings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import com.alexrcq.tvpicturesettings.storage.PreferencesKeys
import com.alexrcq.tvpicturesettings.ui.fragment.base.GlobalSettingsFragment
import com.alexrcq.tvpicturesettings.util.onClick
import com.alexrcq.tvpicturesettings.util.showToast

class MsspVideoPreferencesFragment : GlobalSettingsFragment(R.xml.mssp_video_prefs) {

    private lateinit var videoSettings: VideoSettings
    private val userModeSnapshot = mutableMapOf<String, Int>()
    private val userModeSnapshotKeys = setOf(
        MtkGlobalKeys.PICTURE_BRIGHTNESS,
        MtkGlobalKeys.PICTURE_CONTRAST,
        MtkGlobalKeys.PICTURE_SATURATION,
        MtkGlobalKeys.PICTURE_HUE,
        MtkGlobalKeys.PICTURE_SHARPNESS
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val app = requireActivity().application as App
        videoSettings = app.videoSettings
        setupPreferences()
        handleLocaleSpecifics()
    }

    override fun onStart() {
        super.onStart()
        updateTemperaturePreference()
    }

    private fun setupPreferences() {
        findPreference<Preference>(PreferencesKeys.RESET_TO_DEFAULT)?.onClick(::showResetToDefaultDialog)
        findPreference<ListPreference>(MtkGlobalKeys.PICTURE_MODE)?.setOnPreferenceChangeListener { _, newValue ->
            userModeSnapshot.clear()
            videoSettings.applyPictureMode((newValue as String).toInt())
            false
        }
        userModeSnapshotKeys.forEach { key ->
            findPreference<Preference>(key)?.setOnPreferenceChangeListener { preference, newValue ->
                snapshotUserModeValues(preference.key, newValue as Int)
                videoSettings.applyPictureMode(MtkVideoSettings.PICTURE_MODE_USER)
                true
            }
        }
    }

    private fun updateTemperaturePreference() {
        val app = requireActivity().application as App
        val isWhiteBalanceLocked = app.videoPreferences.isWhiteBalanceLocked
        findPreference<ListPreference>(MtkGlobalKeys.PICTURE_TEMPERATURE)?.apply {
            isEnabled = !isWhiteBalanceLocked
            summaryProvider = SummaryProvider<ListPreference> { preference ->
                if (isWhiteBalanceLocked) {
                    getString(R.string.fixed_see_wb_settings)
                } else if (preference.entry.isNullOrEmpty()) {
                    getString(R.string.custom_see_wb_settings)
                } else {
                    preference.entry
                }
            }
        }
    }

    private fun snapshotUserModeValues(changedKey: String, newValue: Int) {
        userModeSnapshotKeys.forEach { key ->
            userModeSnapshot[key] = if (key == changedKey) {
                newValue
            } else {
                globalSettings.getInt(key, 0)
            }
        }
    }

    override fun onGlobalPreferenceChanged(preference: Preference) {
        val key = preference.key
        val snapshotValue = userModeSnapshot[key]
        if (snapshotValue != null) {
            if (globalSettings.getInt(key, 0) != snapshotValue) {
                globalSettings.putInt(key, snapshotValue)
            }
            return
        }
        super.onGlobalPreferenceChanged(preference)
    }

    private fun handleLocaleSpecifics() {
        when (resources.configuration.locales[0].toLanguageTag()) {
            "ru-RU" -> {
                val app = requireActivity().application as App
                val preferences = app.videoPreferences
                if (preferences.showAboutVideoPrefsRuLocalization) {
                    showAboutRuLocalizationFix()
                    preferences.showAboutVideoPrefsRuLocalization = false
                }
            }
        }
    }

    private fun showResetToDefaultDialog() {
        val dialog = AlertDialog.Builder(context)
            .setMessage(R.string.reset_to_default_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                showToast(getString(R.string.please_wait))
                userModeSnapshot.clear()
                videoSettings.resetToDefault()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.show()
        dialog.getButton(Dialog.BUTTON_NEGATIVE).requestFocus()
    }

    private fun showAboutRuLocalizationFix() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(android.R.string.dialog_alert_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Исправлены опции 'Локальный контраст' и 'Адаптивная подсветка'. " +
                    "В русской локализации системных настроек значения 'Вкл' и 'Выкл' перепутаны местами.")
            .setPositiveButton(android.R.string.ok, null)
            .create()
        dialog.show()
        dialog.getButton(Dialog.BUTTON_POSITIVE).requestFocus()
    }
}