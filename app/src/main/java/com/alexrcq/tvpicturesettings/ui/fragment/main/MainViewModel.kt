package com.alexrcq.tvpicturesettings.ui.fragment.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexrcq.tvpicturesettings.CaptureScreenUseCase
import com.alexrcq.tvpicturesettings.helper.DarkModeManager
import com.alexrcq.tvpicturesettings.R
import com.alexrcq.tvpicturesettings.adblib.AdbClient
import com.alexrcq.tvpicturesettings.storage.DarkModePreferences
import com.alexrcq.tvpicturesettings.storage.TvSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException

class MainViewModel(
    private val darkModeManager: DarkModeManager,
    private val darkModePreferences: DarkModePreferences,
    private val adbClient: AdbClient,
    private val tvSettings: TvSettings,
    private val captureScreen: CaptureScreenUseCase
) : ViewModel() {

    private val _mainState = MutableStateFlow<MainState>(MainState.Idle)
    val mainState = _mainState.asStateFlow()

    private val _mainSideEffect = Channel<MainSideEffect>()
    val mainSideEffect = _mainSideEffect.receiveAsFlow()

    private val darkModeStateHintFlow: Flow<Int> = darkModePreferences.modeFlow.map { currentMode ->
        when (currentMode) {
            DarkModeManager.Mode.OFF -> R.string.click_to_dark_mode
            DarkModeManager.Mode.ONLY_BACKLIGHT -> {
                if (darkModePreferences.threeStepsDarkModeEnabled) {
                    R.string.click_to_turn_on_the_dark_filter
                } else {
                    R.string.click_to_day_mode
                }
            }
            DarkModeManager.Mode.FULL -> R.string.click_to_day_mode
        }
    }

    private val periodicDarkModeHintFlow: Flow<Int> = flow {
        while (true) {
            emit(darkModeStateHintFlow.first())
            delay(CLICK_TO_CHANGE_MODE_HINT_DURATION)
            emit(R.string.hold_to_toggle_extra_dimm_hint)
            delay(HOLD_TO_DIMMING_HINT_DURATION)
        }
    }

    val darkModeHints: Flow<Int> = merge(darkModeStateHintFlow, periodicDarkModeHintFlow)

    val isTvSourceActiveFlow: StateFlow<Boolean> = flow {
        while (true) {
            emit(tvSettings.isTvSourceActive)
            delay(TV_SOURCE_CHECK_INTERVAL)
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val isAdbEnabled: Boolean get() = tvSettings.isAdbEnabled

    fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.ToggleDarkMode -> darkModeManager.toggleMode()
            is MainIntent.EnableDarkModeAndToggleFilter -> enableDarkModeAndToggleFilter()
            is MainIntent.ToggleScreenPower -> tvSettings.toggleScreenPower()
            is MainIntent.CaptureScreenshot -> captureScreenshot()
            is MainIntent.GrantPermission -> grantPermission(intent.permission)
            is MainIntent.ChangeBacklight -> setBacklight(intent.value)
        }
    }

    private fun setBacklight(value: Int) {
        tvSettings.picture.backlight = value
        if (darkModeManager.currentMode == DarkModeManager.Mode.OFF) {
            darkModeManager.setDayBacklight(value)
        }
    }

    private fun enableDarkModeAndToggleFilter() = with(darkModeManager) {
        if (currentMode == DarkModeManager.Mode.FULL) {
            toggleScreenFilter()
        } else {
            setMode(DarkModeManager.Mode.FULL)
        }
    }

    private var captureScreenJob: Job? = null

    private fun captureScreenshot() {
        captureScreenJob?.cancel()
        captureScreenJob = viewModelScope.launch {
            if (!tvSettings.isAdbEnabled) {
                _mainSideEffect.send(MainSideEffect.ShowAdbRequired)
                return@launch
            }
            _mainState.value = MainState.ScreenCapturing
            _mainState.value = MainState.ScreenCaptureFinished(isSuccess = captureScreen())
            delay(CAPTURE_FINISHED_STATE_DURATION)
            _mainState.value = MainState.Idle
        }
    }

    private fun grantPermission(permission: String) {
        _mainState.value = MainState.Loading
        viewModelScope.launch {
            try {
                adbClient.grantPermission(permission)
                _mainState.value = MainState.Idle
            } catch (e: TimeoutException) {
                _mainSideEffect.send(MainSideEffect.ShowAcceptAdbForPermissions)
            } catch (e: Exception) {
                _mainSideEffect.send(MainSideEffect.ShowError(e.message.toString()))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        adbClient.disconnect()
    }

    companion object {
        private const val CLICK_TO_CHANGE_MODE_HINT_DURATION = 7000L
        private const val HOLD_TO_DIMMING_HINT_DURATION = 3500L
        private const val CAPTURE_FINISHED_STATE_DURATION = 3500L
        private const val TV_SOURCE_CHECK_INTERVAL = 2500L

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            darkModeManager: DarkModeManager,
            darkModePreferences: DarkModePreferences,
            adbClient: AdbClient,
            tvSettings: TvSettings,
            captureScreen: CaptureScreenUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MainViewModel(darkModeManager, darkModePreferences, adbClient, tvSettings, captureScreen) as T
        }
    }
}