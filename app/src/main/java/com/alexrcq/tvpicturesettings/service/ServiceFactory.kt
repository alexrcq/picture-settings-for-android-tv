package com.alexrcq.tvpicturesettings.service

import android.content.Context
import android.content.Intent

abstract class ServiceFactory {
    abstract fun getIntent(context: Context): Intent

    fun startForeground(context: Context) {
        context.startForegroundService(getIntent(context))
    }

    fun stop(context: Context) {
        context.stopService(getIntent(context))
    }
}