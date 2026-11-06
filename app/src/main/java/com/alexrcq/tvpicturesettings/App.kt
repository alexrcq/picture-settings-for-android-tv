package com.alexrcq.tvpicturesettings

import android.app.Application
import androidx.preference.PreferenceManager
import com.alexrcq.tvpicturesettings.manager.AppStateInitializer
import com.alexrcq.tvpicturesettings.manager.DarkModeManager
import com.alexrcq.tvpicturesettings.manager.DarkModeScheduler
import com.alexrcq.tvpicturesettings.manager.ScreenScheduler
import com.alexrcq.tvpicturesettings.platform.BacklightControlPolicy
import com.alexrcq.tvpicturesettings.platform.Device
import com.alexrcq.tvpicturesettings.platform.InactivityMonitor
import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.platform.TvInputMonitor
import com.alexrcq.tvpicturesettings.platform.VideoSettings
import com.alexrcq.tvpicturesettings.platform.adb.AdbShell
import com.alexrcq.tvpicturesettings.platform.android.DeveloperSettings
import com.alexrcq.tvpicturesettings.platform.mtk.MtkBacklightControlPolicy
import com.alexrcq.tvpicturesettings.platform.mtk.MtkInactivityMonitor
import com.alexrcq.tvpicturesettings.platform.mtk.MtkScreenSettings
import com.alexrcq.tvpicturesettings.platform.mtk.MtkTvInputMonitor
import com.alexrcq.tvpicturesettings.platform.mtk.MtkVideoSettings
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.MtkGlobalSettingsStore
import com.alexrcq.tvpicturesettings.storage.ScreenControlPreferences
import com.alexrcq.tvpicturesettings.storage.VideoPreferences
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App : Application() {

    val applicationScope = MainScope()

    lateinit var darkModePreferences: DarkModePreferences
    lateinit var videoPreferences: VideoPreferences
    lateinit var screenControlPreferences: ScreenControlPreferences

    lateinit var adbShell: AdbShell

    lateinit var screenSettings: ScreenSettings
    lateinit var videoSettings: VideoSettings
    lateinit var developerSettings: DeveloperSettings
    lateinit var backlightControlPolicy: BacklightControlPolicy
    lateinit var tvInputMonitor: TvInputMonitor
    lateinit var inactivityMonitor: InactivityMonitor

    lateinit var darkModeManager: DarkModeManager
    lateinit var darkModeScheduler: DarkModeScheduler

    lateinit var screenScheduler: ScreenScheduler

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        darkModePreferences = DarkModePreferences(sharedPreferences, resources)
        videoPreferences = VideoPreferences(sharedPreferences, resources)
        screenControlPreferences = ScreenControlPreferences(sharedPreferences, resources)

        adbShell = AdbShell(this)

        val globalSettings = MtkGlobalSettingsStore(contentResolver)
        screenSettings = MtkScreenSettings(globalSettings)
        videoSettings = MtkVideoSettings(globalSettings)
        developerSettings = DeveloperSettings(globalSettings)
        backlightControlPolicy = MtkBacklightControlPolicy(this, globalSettings, applicationScope)
        tvInputMonitor = MtkTvInputMonitor(contentResolver)
        inactivityMonitor = MtkInactivityMonitor()

        darkModeManager = DarkModeManager(darkModePreferences, applicationScope)
        darkModeScheduler = DarkModeScheduler(this, darkModePreferences, applicationScope)

        screenScheduler = ScreenScheduler(this, screenControlPreferences, applicationScope)

        if (Device.isSupported(this)) {
            AppStateInitializer(
                darkModePreferences, darkModeManager, darkModeScheduler, screenScheduler, screenSettings
            ).init()
        }
    }
}
