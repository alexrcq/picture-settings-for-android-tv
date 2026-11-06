package com.alexrcq.tvpicturesettings.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.IBinder
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.alexrcq.tvpicturesettings.App
import com.alexrcq.tvpicturesettings.BroadcastActions
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.manager.DarkModeManager
import com.alexrcq.tvpicturesettings.manager.model.OverlayState
import com.alexrcq.tvpicturesettings.platform.BacklightControlPolicy
import com.alexrcq.tvpicturesettings.platform.Device
import com.alexrcq.tvpicturesettings.platform.InactivityMonitor
import com.alexrcq.tvpicturesettings.platform.ScreenSettings
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.ScreenControlPreferences
import com.alexrcq.tvpicturesettings.ui.OverlayContainer
import com.alexrcq.tvpicturesettings.util.ColorUtils
import com.alexrcq.tvpicturesettings.util.registerReceiverCompat
import com.alexrcq.tvpicturesettings.util.showToast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ScreenManager : LifecycleService() {

    private lateinit var screenControlPrefs: ScreenControlPreferences
    private lateinit var darkModePrefs: DarkModePreferences
    private lateinit var screenSettings: ScreenSettings
    private lateinit var screenDimmerView: View
    private lateinit var blueLightFilterView: View
    private lateinit var overlayContainer: OverlayContainer
    private lateinit var darkModeManager: DarkModeManager
    private lateinit var backlightControlPolicy: BacklightControlPolicy
    private lateinit var inactivityMonitor: InactivityMonitor

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleIntent(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val app = application as App
        screenSettings = app.screenSettings
        darkModePrefs = app.darkModePreferences
        screenControlPrefs = app.screenControlPreferences
        darkModeManager = app.darkModeManager
        backlightControlPolicy = app.backlightControlPolicy
        inactivityMonitor = app.inactivityMonitor
        overlayContainer = OverlayContainer(this)
        screenDimmerView = View(this).apply { setBackgroundColor(Color.BLACK) }
        blueLightFilterView = View(this)
        overlayContainer.addView(blueLightFilterView)
        overlayContainer.addView(screenDimmerView)
        startForeground()
        registerReceiver()
        setupObservers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.getBooleanExtra(EXTRA_AFTER_BOOT, false) == true) {
            onPowerOn()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        overlayContainer.destroy()
        super.onDestroy()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupObservers() {
        darkModeManager.state
            .map { it.overlayState }
            .distinctUntilChanged()
            .onEach(::renderOverlay)
            .launchIn(lifecycleScope)

        darkModeManager.state
            .map { it.targetBacklight }
            .distinctUntilChanged()
            .mapLatest { targetBacklight ->
                backlightControlPolicy.adjustmentAllowedFlow.first { it }
                targetBacklight
            }
            .onEach { screenSettings.backlight = it }
            .launchIn(lifecycleScope)

        darkModeManager.state
            .map { it.activeMode }
            .distinctUntilChanged()
            .drop(1)
            .onEach { mode -> if (darkModePrefs.showModeChanged) showToast(getString(mode.message)) }
            .launchIn(lifecycleScope)

        lifecycleScope.launch {
            screenControlPrefs.screenOffTimeoutMinutes.collectLatest { timeout ->
                inactivityMonitor.startMonitoring(timeout, onTimeout = { screenSettings.isScreenOn = false })
            }
        }
    }

    private fun renderOverlay(state: OverlayState) {
        val shouldShowOverlay = state.shouldShowDimmer || state.shouldShowBlueLightFilter

        if (!shouldShowOverlay) {
            overlayContainer.detachFromWindow()
            return
        }

        if (!overlayContainer.attachToWindow()) return

        screenDimmerView.alpha = (state.dimmingPower / 100f) * MAX_SCREEN_FILTER_ALPHA
        screenDimmerView.isVisible = state.shouldShowDimmer

        if (state.shouldShowBlueLightFilter) {
            blueLightFilterView.setBackgroundColor(ColorUtils.kelvinToRgb(state.blueLightFilterTemp))
            blueLightFilterView.alpha = (state.blueLightFilterPower / 100f) * MAX_SCREEN_FILTER_ALPHA
        }

        blueLightFilterView.isVisible = state.shouldShowBlueLightFilter
    }

    private val legacyActionMap = mapOf(
        BroadcastActions.LEGACY_TOGGLE_DARK_MODE to BroadcastActions.TOGGLE_DARK_MODE,
        BroadcastActions.LEGACY_ENABLE_DARK_MODE to BroadcastActions.ENABLE_DARK_MODE,
        BroadcastActions.LEGACY_DISABLE_DARK_MODE to BroadcastActions.DISABLE_DARK_MODE,
        BroadcastActions.LEGACY_TOGGLE_DIMMING to BroadcastActions.TOGGLE_DIMMING,
        BroadcastActions.LEGACY_ENABLE_DIMMING to BroadcastActions.ENABLE_DIMMING,
        BroadcastActions.LEGACY_DISABLE_DIMMING to BroadcastActions.DISABLE_DIMMING,
        BroadcastActions.LEGACY_CHANGE_DIMMING_POWER to BroadcastActions.SET_DIMMING_POWER,
        BroadcastActions.LEGACY_TOGGLE_SCREEN_POWER to BroadcastActions.TOGGLE_SCREEN_POWER,
    )

    private fun registerReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(BroadcastActions.TOGGLE_DARK_MODE)
            addAction(BroadcastActions.ENABLE_DARK_MODE)
            addAction(BroadcastActions.DISABLE_DARK_MODE)
            addAction(BroadcastActions.TOGGLE_DIMMING)
            addAction(BroadcastActions.ENABLE_DIMMING)
            addAction(BroadcastActions.DISABLE_DIMMING)
            addAction(BroadcastActions.SET_DIMMING_POWER)
            addAction(BroadcastActions.TOGGLE_SCREEN_POWER)
            addAction(BroadcastActions.TOGGLE_BLUE_LIGHT_FILTER)
            legacyActionMap.keys.forEach(::addAction)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiverCompat(broadcastReceiver, intentFilter)
    }

    private fun handleIntent(intent: Intent) {
        val action = legacyActionMap[intent.action] ?: intent.action
        when (action) {
            BroadcastActions.TOGGLE_DARK_MODE -> darkModeManager.toggleMode()
            BroadcastActions.ENABLE_DARK_MODE -> darkModeManager.enableDarkMode()
            BroadcastActions.DISABLE_DARK_MODE -> darkModeManager.disableDarkMode()
            BroadcastActions.TOGGLE_DIMMING -> darkModeManager.toggleScreenDimming()
            BroadcastActions.ENABLE_DIMMING -> darkModeManager.setScreenDimmingEnabled(true)
            BroadcastActions.DISABLE_DIMMING -> darkModeManager.setScreenDimmingEnabled(false)
            BroadcastActions.SET_DIMMING_POWER ->
                darkModeManager.setScreenDimmingPower(
                    intent.getIntExtra(BroadcastActions.EXTRA_FILTER_POWER, 0)
                )
            BroadcastActions.TOGGLE_SCREEN_POWER -> screenSettings.toggleScreenPower()
            BroadcastActions.TOGGLE_BLUE_LIGHT_FILTER -> darkModeManager.toggleBlueLightFilter()
            Intent.ACTION_SCREEN_ON -> onPowerOn()
            Intent.ACTION_SCREEN_OFF -> onPowerOff()
        }
    }

    private fun onPowerOn() {
        if (darkModePrefs.disableDarkModeOnPowerOn) {
            darkModeManager.disableDarkMode()
        }
    }

    private fun onPowerOff() {
        if (Device.isMssp) {
            // Screen must be on at this point
            screenSettings.isScreenOn = true
        }
    }

    private fun startForeground() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object : ServiceFactory() {
        private const val NOTIFICATION_ID = 2
        private const val NOTIFICATION_TITLE = "Screen Manager"
        private const val NOTIFICATION_CONTENT = "Service is running"

        private const val NOTIFICATION_CHANNEL_ID = "screen_manager_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Screen Manager"

        private const val EXTRA_AFTER_BOOT = "after_boot"

        private const val MAX_SCREEN_FILTER_ALPHA = 0.98f

        override fun getIntent(context: Context): Intent = Intent(context, ScreenManager::class.java)

        fun startForeground(context: Context, afterBoot: Boolean = false) {
            val intent = getIntent(context)
            if (afterBoot) intent.putExtra(EXTRA_AFTER_BOOT, true)
            context.startForegroundService(intent)
        }
    }
}