package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.service.DarkModeManager
import com.alexrcq.tvpicturesettings.service.WhiteBalanceFixerService

class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                onSystemAction(context, action)
            }
        }
    }

    private fun onSystemAction(context: Context, action: String) {
        DarkModeManager.startForeground(context, action == Intent.ACTION_BOOT_COMPLETED)
        val isWhiteBalanceFixed = (context.applicationContext as App).picturePreferences.isWhiteBalanceFixed
        if (isWhiteBalanceFixed) {
            WhiteBalanceFixerService.startForeground(context)
        }
    }
}