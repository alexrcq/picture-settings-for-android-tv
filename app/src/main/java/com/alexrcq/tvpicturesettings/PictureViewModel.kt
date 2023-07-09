package com.alexrcq.tvpicturesettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.helper.AppSettings
import com.alexrcq.tvpicturesettings.helper.GlobalSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val CLICK_TO_CHANGE_MODE_HINT_DURATION = 7000L
private const val HOLD_TO_DIMMING_HINT_DURATION = 3500L
private const val CAPTURE_FINISHED_STATE_DURATION = 3500L

class PictureViewModel(
    private val adbShell: AdbShell,
    private val appSettings: AppSettings,
    private val globalSettings: GlobalSettings,
    private val captureScreen: CaptureScreenUseCase
) : ViewModel() {

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Idle)
    val menuState: StateFlow<MenuState>
        get() = _menuState.asStateFlow()

    private val _menuSideEffect = Channel<MenuSideEffect>()
    val menuSideEffect = _menuSideEffect.receiveAsFlow()

    val darkModeHintFlow = flow {
        while (true) {
            val darkModeHintResId = if (appSettings.isDarkModeEnabled) {
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
            is MenuIntent.ToggleDarkMode -> toggleDarkMode()
            is MenuIntent.EnableDarkModeAndToggleFilter -> enableDarkModeAndToggleFilter()
            is MenuIntent.TurnOffScreen -> turnOffScreen()
            is MenuIntent.CaptureScreenshot -> captureScreenshot()
        }
    }

    private fun toggleDarkMode() {
        appSettings.toggleDarkMode()
        _menuSideEffect.trySend(MenuSideEffect.ShowDarkModeStateChanged(appSettings.isDarkModeEnabled))
    }

    private fun enableDarkModeAndToggleFilter() {
        with(appSettings) {
            isDarkModeEnabled = true
            toggleDarkFilter()
        }
        _menuSideEffect.trySend(MenuSideEffect.ShowDarkFilterEnabledMessage(appSettings.isDarkFilterEnabled))
    }

    private fun turnOffScreen() {
        globalSettings.putInt(GlobalSettings.Keys.POWER_PICTURE_OFF, 0)
    }

    private var captureScreenJob: Job? = null

    private fun captureScreenshot() {
        captureScreenJob?.cancel()
        captureScreenJob = viewModelScope.launch {
            if (!globalSettings.isAdbEnabled) {
                _menuSideEffect.send(MenuSideEffect.ShowAdbRequiredMessage)
                return@launch
            }
            _menuState.value = MenuState.ScreenCapturing
            _menuState.value = MenuState.ScreenCaptureFinished(isSuccess = captureScreen())
            delay(CAPTURE_FINISHED_STATE_DURATION)
            _menuState.value = MenuState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbShell.disconnect()
    }

    class Factory(
        private val adbShell: AdbShell,
        private val appSettings: AppSettings,
        private val globalSettings: GlobalSettings,
        private val captureScreen: CaptureScreenUseCase,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PictureViewModel(adbShell, appSettings, globalSettings, captureScreen) as T
        }
    }
}