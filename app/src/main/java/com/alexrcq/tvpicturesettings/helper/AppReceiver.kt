package com.alexrcq.tvpicturesettings.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AppReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AppReceiver", "onReceive, action: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == AutoBacklightManager.ACTION_SCHEDULED_MANAGER_LAUNCH
        ) {
            AutoBacklightManager(context).rescheduleWork()
        }
    }
}