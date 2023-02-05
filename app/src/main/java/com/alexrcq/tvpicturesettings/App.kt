package com.alexrcq.tvpicturesettings

import android.app.Application
import kotlinx.coroutines.MainScope
import timber.log.Timber

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        val applicationScope = MainScope()
    }
}