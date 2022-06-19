package com.alexrcq.tvpicturesettings.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alexrcq.tvpicturesettings.ui.fragment.PicturePreferenceFragment

class AppReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AppReceiver", "onReceive, action: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == ACTION_QUICKBOOT_POWERON ||
            intent.action == AutoBacklightManager.ACTION_SCHEDULED_MANAGER_LAUNCH
        ) {
            AutoBacklightManager(context).rescheduleWork()
            context.sendBroadcast(Intent(PicturePreferenceFragment.ACTION_UPDATE_BACKLIGHT_BAR))
        }
    }

    companion object {
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}