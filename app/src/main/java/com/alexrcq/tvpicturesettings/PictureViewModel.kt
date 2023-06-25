package com.alexrcq.tvpicturesettings

import android.Manifest
import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.helper.AppSettings.Keys.IS_DARK_MODE_ENABLED
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

private const val SCREENSHOTS_PATH = "Screenshots"
private const val CLICK_TO_CHANGE_MODE_HINT_DURATION = 7000L
private const val HOLD_TO_DIMMING_HINT_DURATION = 3500L
private const val CAPTURE_FINISHED_DELAY = 3500L

class PictureViewModel(application: Application) : AndroidViewModel(application) {

    private val adbShell = AdbShell(application)

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Idle)
    val menuState: StateFlow<MenuState>
        get() = _menuState.asStateFlow()

    val isDarkModeEnabledFlow: StateFlow<Boolean> =
        PreferenceManager.getDefaultSharedPreferences(application)
            .getBooleanFlow(IS_DARK_MODE_ENABLED)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val darkModeHintFlow = flow {
        while (true) {
            val isDarkModeEnabled = isDarkModeEnabledFlow.value
            val darkModeHintResId = if (isDarkModeEnabled) {
                R.string.click_to_day_mode
            } else {
                R.string.click_to_dark_mode
            }
            emit(darkModeHintResId)
            delay(CLICK_TO_CHANGE_MODE_HINT_DURATION)
            emit(R.string.hold_to_toggle_extra_dimm_hint)
            delay(HOLD_TO_DIMMING_HINT_DURATION)
        }
    }

    fun processIntent(intent: MenuIntent) {
        when (intent) {
            is MenuIntent.ChangeMenuState -> _menuState.value = intent.menuState
            is MenuIntent.CaptureScreenshot -> captureScreenshot()
        }
    }

    private var captureScreenJob: Job? = null

    private fun captureScreenshot() {
        captureScreenJob?.cancel()
        captureScreenJob = viewModelScope.launch {
            _menuState.value = MenuState.ScreenCapturing
            _menuState.value = MenuState.ScreenCaptureFinished(isSuccess = captureScreenWithAdb())
            delay(CAPTURE_FINISHED_DELAY)
            _menuState.value = MenuState.Idle
        }
    }

    private suspend fun captureScreenWithAdb(): Boolean {
        try {
            withContext(Dispatchers.IO) {
                adbShell.connect()
                adbShell.grantPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                adbShell.grantPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                adbShell.captureScreen(
                    saveDir = prepareDir(
                        "${Environment.getExternalStorageDirectory().path}/$SCREENSHOTS_PATH"
                    )
                )
            }
            return true
        } catch (e: Exception) {
            Timber.e(e, "Screen capture failed")
            return false
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbShell.disconnect()
    }
}