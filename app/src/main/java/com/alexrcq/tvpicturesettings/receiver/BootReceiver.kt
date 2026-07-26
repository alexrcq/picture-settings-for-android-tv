package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.service.MtkWhiteBalanceLocker
import com.alexrcq.tvpicturesettings.service.ScreenManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ScreenManager.startForeground(context, intent.action == Intent.ACTION_BOOT_COMPLETED)
        val isWhiteBalanceLocked = (context.applicationContext as App).videoPreferences.isWhiteBalanceLocked
        if (isWhiteBalanceLocked) {
            MtkWhiteBalanceLocker.startForeground(context)
        }
    }
}