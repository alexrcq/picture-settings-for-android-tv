package com.alexrcq.tvpicturesettings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alexrcq.tvpicturesettings.service.AutoBacklightService

class AppReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AppReceiver", "onReceive, action: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == ACTION_QUICKBOOT_POWERON ||
            intent.action == AutoBacklightService.ACTION_SCHEDULED_SERVICE_LAUNCH
        ) {
            startAutoBacklightService(context)
        }
    }

    private fun startAutoBacklightService(context: Context) {
        val launchIntent = Intent(context, AutoBacklightService::class.java).apply {
            action = ACTION_RECEIVER_LAUNCHED
        }
        context.startForegroundService(launchIntent)
    }

    companion object {
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
        const val ACTION_RECEIVER_LAUNCHED = "com.alexrcq.tvpicturesettings.RECEIVER_LAUNCHED_ACTION"
    }
}