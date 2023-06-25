package com.alexrcq.tvpicturesettings

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.alexrcq.tvpicturesettings.service.DarkFilterService
import com.alexrcq.tvpicturesettings.helper.AppSettings
import com.alexrcq.tvpicturesettings.helper.GlobalSettings
import com.alexrcq.tvpicturesettings.helper.GlobalSettings.Keys.PICTURE_BACKLIGHT
import com.alexrcq.tvpicturesettings.helper.GlobalSettingsWrapper
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App : Application() {

    lateinit var appSettings: AppSettings
    lateinit var globalSettings: GlobalSettings

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DarkFilterService.ACTION_SERVICE_CONNECTED) {
                initDarkFilter()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        globalSettings = GlobalSettingsWrapper(contentResolver)
        initAppSettings()
        registerReceiver(
            broadcastReceiver,
            IntentFilter(DarkFilterService.ACTION_SERVICE_CONNECTED)
        )
    }

    private fun initAppSettings() {
        appSettings = AppSettings(this).apply {
            if (dayBacklight !in 0..100) {
                dayBacklight = globalSettings.getInt(PICTURE_BACKLIGHT, 0)
            }
        }
    }

    private fun initDarkFilter() {
        with(appSettings) {
            DarkFilterService.sharedInstance!!.darkFilter.apply {
                isEnabled = isDarkFilterEnabled || isDarkModeEnabled && isAdditionalDimmingEnabled
                alpha = darkFilterPower / 100f
            }
        }
    }

    companion object {
        val applicationScope = MainScope()
    }
}