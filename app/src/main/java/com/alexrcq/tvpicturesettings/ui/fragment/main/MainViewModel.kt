package com.alexrcq.tvpicturesettings.ui.fragment.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexrcq.tvpicturesettings.domain.usecase.CaptureScreenUseCase
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.adblib.AdbClient
import com.alexrcq.tvpicturesettings.domain.DarkModeManager
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.domain.TvSettings
import com.alexrcq.tvpicturesettings.util.DarkModeHintProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException

class MainViewModel(
    private val darkModePreferences: DarkModePreferences,
    private val darkModeHintProvider: DarkModeHintProvider,
    private val adbClient: AdbClient,
    private val captureScreen: CaptureScreenUseCase,
    private val tvSettings: TvSettings,
    private val darkModeManager: DarkModeManager
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private val _sideEffectChannel = Channel<MainSideEffect>()
    val sideEffectFlow = _sideEffectChannel.receiveAsFlow()

    private val clickToNextModeHintFlow: StateFlow<Int> =
        darkModePreferences.modeFlow.map { mode -> darkModeHintProvider.getClickToNextModeHint(mode) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            initialValue = darkModeHintProvider.getClickToNextModeHint(darkModeManager.currentMode)
        )

    private val holdToToggleDimmingPeriodicHintFlow: Flow<Int> = flow {
        while (true) {
            emit(darkModeHintProvider.getClickToNextModeHint(darkModeManager.currentMode))
            delay(HOW_TO_SWITCH_MODE_HINT_DURATION)
            emit(R.string.hold_to_toggle_extra_dimm_hint)
            delay(HOLD_TO_DIMMING_HINT_DURATION)
        }
    }

    val darkModeHints: Flow<Int> = merge(clickToNextModeHintFlow, holdToToggleDimmingPeriodicHintFlow)

    val isTvSourceInactiveFlow: SharedFlow<Boolean> = tvSettings.tvSourceFlow().map { source -> source == null }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val isAdbEnabled: Boolean get() = tvSettings.isAdbEnabled()

    val isBacklightAdjustAllowedFlow: SharedFlow<Boolean> = tvSettings.picture.backlightAdjustAllowedFlow

    fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.ToggleDarkMode -> darkModeManager.toggleMode()
            is MainIntent.ToggleFullDarkMode -> toggleFullDarkMode()
            is MainIntent.ToggleScreenPower -> tvSettings.toggleScreenPower()
            is MainIntent.CaptureScreenshot -> captureScreenshot()
            is MainIntent.GrantPermissions -> grantPermissions(intent.permissions)
            is MainIntent.ChangeBacklight -> setBacklight(intent.value)
        }
    }

    private fun setBacklight(value: Int) {
        tvSettings.picture.backlight = value
        if (darkModeManager.currentMode == DarkModeManager.Mode.OFF) {
            darkModePreferences.dayBacklight = value
        }
    }

    private fun toggleFullDarkMode() = with(darkModeManager) {
        if (currentMode == DarkModeManager.Mode.FULL) {
            toggleScreenDimming()
        } else {
            currentMode = DarkModeManager.Mode.FULL
        }
    }

    private var captureScreenJob: Job? = null

    private fun captureScreenshot() {
        captureScreenJob?.cancel()
        captureScreenJob = viewModelScope.launch {
            if (!isAdbEnabled) {
                _sideEffectChannel.send(MainSideEffect.ShowAdbRequired)
                return@launch
            }
            _uiStateFlow.value = MainUiState.ScreenCapturing
            _uiStateFlow.value = MainUiState.ScreenCaptureFinished(isSuccess = captureScreen())
            delay(SCREEN_CAPTURE_FINISHED_STATE_DURATION)
            _uiStateFlow.value = MainUiState.Idle
        }
    }

    private fun grantPermissions(permissions: List<String>) {
        _uiStateFlow.value = MainUiState.Loading
        viewModelScope.launch {
            try {
                adbClient.grantPermissions(permissions)
                _sideEffectChannel.send(MainSideEffect.PermissionsGranted)
                _uiStateFlow.value = MainUiState.Idle
            } catch (e: TimeoutException) {
                _sideEffectChannel.send(MainSideEffect.ShowGrantPermissions(permissions))
            } catch (e: Exception) {
                _sideEffectChannel.send(MainSideEffect.ShowError(e.message.toString()))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbClient.disconnect()
    }

    companion object {
        private const val HOW_TO_SWITCH_MODE_HINT_DURATION = 7000L
        private const val HOLD_TO_DIMMING_HINT_DURATION = 3500L
        private const val SCREEN_CAPTURE_FINISHED_STATE_DURATION = 3500L

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            darkModePreferences: DarkModePreferences,
            darkModeHintProvider: DarkModeHintProvider,
            adbClient: AdbClient,
            captureScreen: CaptureScreenUseCase,
            tvSettings: TvSettings,
            darkModeManager: DarkModeManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(
                darkModePreferences, darkModeHintProvider, adbClient, captureScreen, tvSettings, darkModeManager
            ) as T
        }
    }
}