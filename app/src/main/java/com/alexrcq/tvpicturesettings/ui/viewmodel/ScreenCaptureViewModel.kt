package com.alexrcq.tvpicturesettings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexrcq.tvpicturesettings.platform.TvInputMonitor
import com.alexrcq.tvpicturesettings.platform.adb.AdbShell
import com.alexrcq.tvpicturesettings.platform.android.DeveloperSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface ScreenCaptureState {
    object Idle : ScreenCaptureState
    object Capturing : ScreenCaptureState
    class Completed(val success: Boolean) : ScreenCaptureState
}

sealed interface ScreenCaptureSideEffect {
    object AdbRequired : ScreenCaptureSideEffect
    class Error(val message: String) : ScreenCaptureSideEffect
}

class ScreenCaptureViewModel(
    private val adbShell: AdbShell, private val developerSettings: DeveloperSettings, tvInputMonitor: TvInputMonitor
) : ViewModel() {

    private val _state = MutableStateFlow<ScreenCaptureState>(ScreenCaptureState.Idle)
    val state: StateFlow<ScreenCaptureState> = _state.asStateFlow()

    private val _sideEffects = Channel<ScreenCaptureSideEffect>()
    val sideEffects = _sideEffects.receiveAsFlow()

    val isScreenCaptureAvailable: SharedFlow<Boolean> = tvInputMonitor.activeInput.map { it == null }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    fun captureScreen() {
        if (state.value !is ScreenCaptureState.Idle) return

        viewModelScope.launch {
            if (!developerSettings.isAdbEnabled) {
                _sideEffects.send(ScreenCaptureSideEffect.AdbRequired)
                return@launch
            }

            _state.value = ScreenCaptureState.Capturing

            try {
                val success = adbShell.captureScreen()
                _state.value = ScreenCaptureState.Completed(success)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.value = ScreenCaptureState.Completed(false)
                _sideEffects.send(ScreenCaptureSideEffect.Error("ADB error: ${e.message}"))
            }

            delay(COMPLETED_STATE_DURATION_MS)
            _state.value = ScreenCaptureState.Idle
        }
    }

    override fun onCleared() {
        adbShell.disconnect()
    }

    companion object {
        private const val COMPLETED_STATE_DURATION_MS = 3500L

        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            adbShell: AdbShell, developerSettings: DeveloperSettings, tvInputMonitor: TvInputMonitor
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ScreenCaptureViewModel(adbShell, developerSettings, tvInputMonitor) as T
            }
    }
}