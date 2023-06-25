package com.alexrcq.tvpicturesettings.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber

class UserActionBroadcastReceiver(val handleAction: (action: String?) -> Unit): BroadcastReceiver() {

    val intentFilter = IntentFilter().apply {
        addAction(ACTION_TOGGLE_DARK_MODE)
        addAction(ACTION_ENABLE_DARK_MODE)
        addAction(ACTION_DISABLE_DARK_MODE)
        addAction(ACTION_TOGGLE_FILTER)
        addAction(ACTION_ENABLE_FILTER)
        addAction(ACTION_DISABLE_FILTER)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("${intent.action}")
        handleAction(intent.action)
    }

    companion object {
        const val ACTION_TOGGLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE"
        const val ACTION_ENABLE_DARK_MODE = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_DARK_MODE"
        const val ACTION_DISABLE_DARK_MODE =
            "com.alexrcq.tvpicturesettings.ACTION_DISABLE_DARK_MODE"

        const val ACTION_TOGGLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_FILTER"
        const val ACTION_ENABLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_FILTER"
        const val ACTION_DISABLE_FILTER = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_FILTER"
    }
}