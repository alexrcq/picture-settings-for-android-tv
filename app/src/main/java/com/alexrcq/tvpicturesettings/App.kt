package com.alexrcq.tvpicturesettings

import android.app.Application
import androidx.preference.PreferenceManager
import com.alexrcq.tvpicturesettings.adblib.AdbClient
import com.alexrcq.tvpicturesettings.adblib.AdbShellCommandExecutor
import com.alexrcq.tvpicturesettings.domain.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsWrapper
import com.alexrcq.tvpicturesettings.domain.MtkPictureSettings
import com.alexrcq.tvpicturesettings.domain.MtkTvSettings
import com.alexrcq.tvpicturesettings.storage.PicturePreferences
import com.alexrcq.tvpicturesettings.domain.TvSettings
import com.alexrcq.tvpicturesettings.ui.OverlayManager
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App : Application() {

    lateinit var adbClient: AdbClient
    lateinit var darkModePreferences: DarkModePreferences
    lateinit var darkModeManager: DarkModeManager
    lateinit var picturePreferences: PicturePreferences
    lateinit var tvSettings: TvSettings
    lateinit var overlayManager: OverlayManager

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        darkModePreferences = DarkModePreferences(sharedPreferences, resources)
        picturePreferences = PicturePreferences(sharedPreferences, resources)
        adbClient = AdbShellCommandExecutor(this)
        val globalSettings = GlobalSettingsWrapper(contentResolver)
        val pictureSettings = MtkPictureSettings(this, globalSettings)
        tvSettings = MtkTvSettings(contentResolver, globalSettings, pictureSettings)
        overlayManager = OverlayManager(this)
        darkModeManager = DarkModeManager(darkModePreferences, pictureSettings)
    }

    companion object {
        val applicationScope = MainScope()
    }
}