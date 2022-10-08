package com.alexrcq.tvpicturesettings

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.appPreferences
import com.alexrcq.tvpicturesettings.ui.FullScreenDarkFilter
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


class DarkModeManager : AccessibilityService() {

    private lateinit var pictureSettings: PictureSettings
    lateinit var darkFilter: FullScreenDarkFilter

    private val screenOnBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("${intent.action}")
            if (intent.action != ACTION_SCREEN_ON) return
            if (appPreferences.isDayModeAfterScreenOnEnabled) {
                isDarkModeEnabled = false
            }
        }
    }

    private var isDarkModeEnabled: Boolean
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
        pictureSettings = PictureSettings(applicationContext)
        darkFilter = FullScreenDarkFilter(this).apply {
            alpha = appPreferences.darkFilterPower / 100f
        }
        registerReceiver(screenOnBroadcastReceiver, IntentFilter(ACTION_SCREEN_ON))
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

    fun setDayModeTime(dayModeTime: String) {
        ChangeModeAlarmManager().setRepeatingAlarm(DAY_MODE_ALARM_ID, dayModeTime)
    }

    fun setDarkModeTime(darkModeTime: String) {
        ChangeModeAlarmManager().setRepeatingAlarm(DARK_MODE_ALARM_ID, darkModeTime)
    }

    fun setAutoDarkModeEnabled(isAutoDarkModeEnabled: Boolean) {
        val changeModeAlarmManager = ChangeModeAlarmManager()
        if (isAutoDarkModeEnabled) {
            if (!isDarkModeEnabled) {
                appPreferences.dayBacklight = pictureSettings.backlight
            }
            changeModeAlarmManager.setRepeatingAlarm(DAY_MODE_ALARM_ID, appPreferences.dayModeTime)
            changeModeAlarmManager.setRepeatingAlarm(
                DARK_MODE_ALARM_ID,
                appPreferences.darkModeTime
            )
            return
        }
        isDarkModeEnabled = false
        changeModeAlarmManager.cancelAllAlarms()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        unregisterReceiver(screenOnBroadcastReceiver)
        sharedInstance = null
        return super.onUnbind(intent)
    }

    private inner class ChangeModeAlarmManager {

        private val alarmManager =
            application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun setRepeatingAlarm(modeId: Int, time: String) {
            val changeModeTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time))
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                changeModeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                AlarmManager.INTERVAL_DAY,
                getPendingIntent(modeId)
            )
        }

        private fun getPendingIntent(modeId: Int): PendingIntent? = PendingIntent.getBroadcast(
            applicationContext,
            modeId,
            getIntent(applicationContext, modeId),
            PendingIntent.FLAG_IMMUTABLE
        )

        private fun getIntent(context: Context, modeId: Int): Intent =
            Intent(context, ChangeBacklightModeBroadcastReceiver::class.java).apply {
                action = ACTION_CHANGE_BACKLIGHT_MODE
                putExtra(EXTRA_BACKLIGHT_MODE_ALARM_ID, modeId)
            }

        fun cancelAllAlarms() {
            alarmManager.cancel(getPendingIntent(DAY_MODE_ALARM_ID))
            alarmManager.cancel(getPendingIntent(DARK_MODE_ALARM_ID))
        }
    }

    class ChangeBacklightModeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("onReceive, action: ${intent.action}")
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> requireInstance().onBootCompleted()
                ACTION_CHANGE_BACKLIGHT_MODE -> {
                    val alarmId = intent.getIntExtra(EXTRA_BACKLIGHT_MODE_ALARM_ID, -1)
                    requireInstance().handleModeChangeAlarm(alarmId)
                }
            }
        }
    }

    private fun onBootCompleted() {
        if (appPreferences.isDayModeAfterScreenOnEnabled) {
            isDarkModeEnabled = false
        }
        ChangeModeAlarmManager().apply {
            setRepeatingAlarm(DARK_MODE_ALARM_ID, appPreferences.darkModeTime)
            setRepeatingAlarm(DAY_MODE_ALARM_ID, appPreferences.dayModeTime)
        }
    }

    private fun handleModeChangeAlarm(alarmId: Int) {
        when (alarmId) {
            DARK_MODE_ALARM_ID -> isDarkModeEnabled = true
            DAY_MODE_ALARM_ID -> isDarkModeEnabled = false
        }
    }

    companion object {
        const val ACTION_SERVICE_CONNECTED = "ACTION_DARK_MANAGER_CONNECTED"
        const val ACTION_CHANGE_BACKLIGHT_MODE =
            "com.alerxrcq.tvpicturesettings.DarkModeManager.ACTION_CHANGE_BACKLIGHT_MODE"
        const val EXTRA_BACKLIGHT_MODE_ALARM_ID = "backlightModeAlarmId"
        const val DAY_MODE_ALARM_ID = 6700
        const val DARK_MODE_ALARM_ID = 6701

        @SuppressLint("StaticFieldLeak")
        var sharedInstance: DarkModeManager? = null

        fun requireInstance(): DarkModeManager {
            return sharedInstance ?: throw java.lang.IllegalStateException(
                "${DarkModeManager::class.java.simpleName} is not initialized yet"
            )
        }
    }
}