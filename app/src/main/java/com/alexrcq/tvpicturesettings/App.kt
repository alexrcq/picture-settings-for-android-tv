package com.alexrcq.tvpicturesettings

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import androidx.preference.PreferenceManager
import com.alexrcq.tvpicturesettings.adblib.AdbClient
import com.alexrcq.tvpicturesettings.adblib.AdbShellCommandExecutor
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.helper.DarkModeManagerImpl
import com.alexrcq.tvpicturesettings.service.ScreenFilterService
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.TvSettings
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsWrapper
import com.alexrcq.tvpicturesettings.storage.MtkPictureSettings
import com.alexrcq.tvpicturesettings.storage.MtkTvSettings
import com.alexrcq.tvpicturesettings.storage.PicturePreferences
import com.alexrcq.tvpicturesettings.storage.SharedPreferencesStore
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App : Application() {

    lateinit var adbClient: AdbClient
    lateinit var tvSettings: TvSettings
    lateinit var darkModeManager: DarkModeManager
    lateinit var darkModePreferences: DarkModePreferences
    lateinit var picturePreferences: PicturePreferences

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ScreenFilterService.ACTION_SERVICE_CONNECTED) {
                initScreenFilter()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val preferencesStore = SharedPreferencesStore(PreferenceManager.getDefaultSharedPreferences(this))
        darkModePreferences = DarkModePreferences(preferencesStore)
        picturePreferences = PicturePreferences(preferencesStore)
        adbClient = AdbShellCommandExecutor(this)
        val globalSettings = GlobalSettingsWrapper(contentResolver)
        tvSettings = MtkTvSettings(contentResolver, globalSettings, MtkPictureSettings(globalSettings))
        darkModeManager = DarkModeManagerImpl(this, tvSettings.picture, darkModePreferences)
        registerReceiver(broadcastReceiver, IntentFilter(ScreenFilterService.ACTION_SERVICE_CONNECTED))
    }

    private fun initScreenFilter() {
        ScreenFilterService.sharedInstance!!.screenFilter.apply {
            isEnabled = darkModePreferences.isScreenFilterEnabled
            setColor(Color.BLACK)
            setPower(darkModePreferences.screenFilterPower)
        }
    }

    companion object {
        val applicationScope = MainScope()
    }
}