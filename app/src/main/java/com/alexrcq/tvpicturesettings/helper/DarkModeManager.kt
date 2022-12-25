package com.alexrcq.tvpicturesettings.helper

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.storage.AppPreferences
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.ui.FullScreenDarkFilter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DarkModeManager : AccessibilityService() {

    @Inject
    lateinit var pictureSettings: PictureSettings

    @Inject
    lateinit var appPreferences: AppPreferences

    lateinit var darkFilter: FullScreenDarkFilter

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("${intent.action}")
            handleBroadcastAction(intent.action)
        }
    }

    var isDarkModeEnabled: Boolean
        get() = appPreferences.isDarkModeEnabled
        set(isDarkModeEnabled) {
            val oldValue = appPreferences.isDarkModeEnabled
            if (isDarkModeEnabled == oldValue) return
            appPreferences.isDarkModeEnabled = isDarkModeEnabled
            handleBacklight()
            darkFilter.isEnabled = appPreferences.isDarkFilterEnabled && isDarkModeEnabled
            showModeChangedToast()
        }

    private fun handleBacklight() {
        pictureSettings.backlight = if (isDarkModeEnabled) {
            appPreferences.nightBacklight
        } else {
            appPreferences.dayBacklight
        }
    }

    private fun handleBroadcastAction(action: String?) {
        when (action) {
            ACTION_SCREEN_ON -> onScreenOn()
            ACTION_TOGGLE_MODE -> toggleDarkmode()
            ACTION_ENABLE_MODE -> isDarkModeEnabled = true
            ACTION_DISABLE_MODE -> isDarkModeEnabled = false
        }
    }

    private fun showModeChangedToast() {
        val messageResId: Int = if (isDarkModeEnabled) {
            R.string.dark_mode_activated
        } else {
            R.string.day_mode_activated
        }
        Toast.makeText(applicationContext, messageResId, Toast.LENGTH_SHORT).show()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("onServiceConnected")
        darkFilter = FullScreenDarkFilter(this).apply {
            alpha = appPreferences.darkFilterPower / 100f
            isEnabled = appPreferences.isDarkFilterEnabled && appPreferences.isDarkModeEnabled
        }
        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(ACTION_SCREEN_ON)
            addAction(ACTION_TOGGLE_MODE)
            addAction(ACTION_ENABLE_MODE)
            addAction(ACTION_DISABLE_MODE)
        })
        sharedInstance = this
        application.sendBroadcast(
            Intent(ACTION_SERVICE_CONNECTED).apply {
                `package` = application.packageName
            }
        )
    }

    fun toggleDarkmode() {
        isDarkModeEnabled = !isDarkModeEnabled
    }

    private fun onScreenOn() {
        if (appPreferences.isDayModeAfterScreenOnEnabled) {
            isDarkModeEnabled = false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        unregisterReceiver(broadcastReceiver)
        sharedInstance = null
        return super.onUnbind(intent)
    }

    companion object {
        const val ACTION_SERVICE_CONNECTED =
            "com.alexrcq.tvpicturesettings.ACTION_DARK_MODE_MANAGER_CONNECTED"
        const val ACTION_TOGGLE_MODE = "com.alexrcq.tvpicturesettings.ACTION_TOGGLE_DARK_MODE"
        const val ACTION_ENABLE_MODE = "com.alexrcq.tvpicturesettings.ACTION_ENABLE_DARK_MODE"
        const val ACTION_DISABLE_MODE = "com.alexrcq.tvpicturesettings.ACTION_DISABLE_DARK_MODE"

        var sharedInstance: DarkModeManager? = null

        fun requireInstance(): DarkModeManager {
            return sharedInstance ?: error(
                "${DarkModeManager::class.java.simpleName} is not initialized yet"
            )
        }
    }
}