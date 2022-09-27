package com.alexrcq.tvpicturesettings.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.*
import android.content.Intent.ACTION_SCREEN_ON
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.content.edit
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.service.DarkModeManager.Preferences.Keys.DARK_FILTER_ALPHA
import com.alexrcq.tvpicturesettings.service.DarkModeManager.Preferences.Keys.DAY_BACKLIGHT
import com.alexrcq.tvpicturesettings.service.DarkModeManager.Preferences.Keys.IS_DARK_FILTER_ON_DARK_MODE_ENABLED
import com.alexrcq.tvpicturesettings.service.DarkModeManager.Preferences.Keys.IS_DARK_MODE_ENABLED
import com.alexrcq.tvpicturesettings.service.DarkModeManager.Preferences.Keys.IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED
import com.alexrcq.tvpicturesettings.service.DarkModeManager.Preferences.Keys.NIGHT_BACKLIGHT
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import timber.log.Timber


class DarkModeManager : AccessibilityService() {

    private lateinit var pictureSettings: PictureSettings
    private lateinit var preferences: Preferences

    private var darkFilterView: View? = null

    private val screenOnBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("${intent.action}")
            if (intent.action != ACTION_SCREEN_ON) return
            if (isDayModeAfterScreenOnEnabled) {
                isDarkModeEnabled = false
            }
        }
    }

    var isDarkModeEnabled: Boolean
        get() = preferences.isDarkModeEnabled
        set(newValue) {
            val oldValue = preferences.isDarkModeEnabled
            preferences.isDarkModeEnabled = newValue
            if (oldValue == newValue) {
                return
            }
            pictureSettings.backlight = if (isDarkModeEnabled) {
                nightBacklight
            } else {
                dayBacklight
            }
            isDarkFilterEnabled = isDarkFilterOnDarkModeEnabled && isDarkModeEnabled
            showModeChangedToast()
        }

    private var isDarkFilterEnabled: Boolean = false
        set(enabled) {
            field = enabled
            if (enabled) {
                enableDarkFilter()
                return
            }
            disableDarkFilter()
        }

    var isDarkFilterOnDarkModeEnabled: Boolean
        get() = preferences.isDarkFilterAtDarkModeEnabled
        set(enabled) {
            preferences.isDarkFilterAtDarkModeEnabled = enabled
            if (isDarkModeEnabled) {
                isDarkFilterEnabled = enabled
            }
        }

    var isDayModeAfterScreenOnEnabled: Boolean
        get() = preferences.isDayModeAfterScreenOnEnabled
        set(enabled) {
            preferences.isDayModeAfterScreenOnEnabled = enabled
        }

    var nightBacklight: Int
        get() = preferences.nightBacklight
        set(nightBacklight) {
            preferences.nightBacklight = nightBacklight
            if (isDarkModeEnabled) {
                pictureSettings.backlight = nightBacklight
            }
        }

    var dayBacklight: Int
        get() = preferences.dayBacklight
        set(dayBacklight) {
            preferences.dayBacklight = dayBacklight
            // FIXME:
            if (isDarkModeEnabled) {
                pictureSettings.backlight = nightBacklight
            }
        }

    var darkFilterAlpha: Float
        get() = preferences.darkFilterAlpha
        set(value) {
            preferences.darkFilterAlpha = value
            darkFilterView?.alpha = value
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
        preferences = Preferences(applicationContext)
        darkFilterView = View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = darkFilterAlpha
        }
        registerReceiver(screenOnBroadcastReceiver, IntentFilter(ACTION_SCREEN_ON))
        sharedInstance = this
        application.sendBroadcast(
            Intent(ACTION_SERVICE_CONNECTED).apply {
                `package` = application.packageName
            }
        )
        with(preferences) {
            if (firstLaunch) {
                dayBacklight = pictureSettings.backlight
                firstLaunch = false
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun toggleDarkmode() {
        isDarkModeEnabled = !isDarkModeEnabled
    }

    private fun enableDarkFilter() {
        val layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        try {
            windowManager.addView(darkFilterView, layoutParams)
        } catch (e: IllegalStateException) {
            Timber.d("the dark filter view has already added", e)
        }
    }

    private fun disableDarkFilter() {
        if (darkFilterView?.windowToken != null) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(darkFilterView)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        unregisterReceiver(screenOnBroadcastReceiver)
        sharedInstance = null
        return super.onUnbind(intent)
    }

    class Preferences(context: Context) {
        private var preferences: SharedPreferences =
            context.applicationContext.getSharedPreferences("dark_mode_prefs", MODE_PRIVATE)

        var firstLaunch: Boolean
            get() = preferences.getBoolean("first_launch", true)
            set(value) {
                preferences.edit {
                    putBoolean("first_launch", value)
                }
            }

        var isDarkModeEnabled: Boolean
            get() = preferences.getBoolean(IS_DARK_MODE_ENABLED, false)
            set(value) {
                preferences.edit {
                    putBoolean(IS_DARK_MODE_ENABLED, value)
                }
            }

        var isDayModeAfterScreenOnEnabled: Boolean
            get() = preferences.getBoolean(IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED, false)
            set(value) {
                preferences.edit {
                    putBoolean(IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED, value)
                }
            }

        var nightBacklight: Int
            get() = preferences.getInt(NIGHT_BACKLIGHT, 0)
            set(value) {
                preferences.edit {
                    putInt(NIGHT_BACKLIGHT, value)
                }
            }

        var dayBacklight: Int
            get() = preferences.getInt(DAY_BACKLIGHT, 100)
            set(value) {
                preferences.edit {
                    putInt(DAY_BACKLIGHT, value)
                }
            }

        var isDarkFilterAtDarkModeEnabled: Boolean
            get() = preferences.getBoolean(IS_DARK_FILTER_ON_DARK_MODE_ENABLED, false)
            set(value) {
                preferences.edit {
                    putBoolean(IS_DARK_FILTER_ON_DARK_MODE_ENABLED, value)
                }
            }

        var darkFilterAlpha: Float
            get() = preferences.getFloat(DARK_FILTER_ALPHA, 0.5f)
            set(value) {
                preferences.edit {
                    putFloat(DARK_FILTER_ALPHA, value)
                }
            }

        object Keys {
            const val IS_DAY_MODE_AFTER_SCREEN_ON_ENABLED =
                "is_enable_day_mode_after_screen_on_enabled"
            const val IS_DARK_MODE_ENABLED = "is_dark_mode_enabled"
            const val IS_DARK_FILTER_ON_DARK_MODE_ENABLED = "is_dark_filter_enabled"
            const val DAY_BACKLIGHT = "day_backlight"
            const val NIGHT_BACKLIGHT = "night_backlight"
            const val DARK_FILTER_ALPHA = "dark_filter_alpha"
        }
    }


    companion object {
        const val ACTION_SERVICE_CONNECTED = "ACTION_DARK_MANAGER_CONNECTED"

        @SuppressLint("StaticFieldLeak")
        var sharedInstance: DarkModeManager? = null

        fun requireInstance(): DarkModeManager {
            return sharedInstance ?: throw java.lang.IllegalStateException(
                "${DarkModeManager::class.java.simpleName} is not initialized yet"
            )
        }
    }
}