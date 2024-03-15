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
import com.alexrcq.tvpicturesettings.service.ScreenFilterService
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.GlobalSettingsWrapper
import com.alexrcq.tvpicturesettings.storage.MtkPictureSettings
import com.alexrcq.tvpicturesettings.storage.MtkTvSettings
import com.alexrcq.tvpicturesettings.storage.PicturePreferences
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App : Application() {

    lateinit var adbClient: AdbClient
    lateinit var darkModePreferences: DarkModePreferences
    lateinit var picturePreferences: PicturePreferences
    lateinit var tvSettingsRepository: TvSettingsRepository

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        darkModePreferences = DarkModePreferences(sharedPreferences)
        picturePreferences = PicturePreferences(sharedPreferences)
        adbClient = AdbShellCommandExecutor(this)
        val globalSettings = GlobalSettingsWrapper(contentResolver)
        val tvSettings = MtkTvSettings(contentResolver, globalSettings, MtkPictureSettings(globalSettings))
        tvSettingsRepository = TvSettingsRepository(this, tvSettings)
        registerReceiver(broadcastReceiver, IntentFilter(ScreenFilterService.ACTION_SERVICE_CONNECTED))
    }

    private fun initScreenFilter() {
        ScreenFilterService.sharedInstance!!.screenFilter.apply {
            setEnabled(darkModePreferences.isScreenFilterEnabled)
            setColor(Color.BLACK)
            setPower(darkModePreferences.screenFilterPower)
        }
    }

    companion object {
        val applicationScope = MainScope()
    }
}