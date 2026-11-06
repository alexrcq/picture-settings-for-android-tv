package com.alexrcq.tvpicturesettings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alexrcq.tvpicturesettings.platform.android.DeveloperSettings
import com.alexrcq.tvpicturesettings.platform.android.PermissionGranter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeoutException

sealed interface SetupState {
    object Idle : SetupState
    object Processing : SetupState
}

sealed interface SetupSideEffect {
    object AdbRequired : SetupSideEffect
    class GrantPermissionsTimeout(val permissions: List<String>) : SetupSideEffect
    object PermissionsGranted : SetupSideEffect
    class Error(val message: String) : SetupSideEffect
}

class SetupViewModel(
    private val permissionGranter: PermissionGranter, private val developerSettings: DeveloperSettings
) : ViewModel() {

    private val _state = MutableStateFlow<SetupState>(SetupState.Idle)
    val state: StateFlow<SetupState> = _state.asStateFlow()

    private val _sideEffects = Channel<SetupSideEffect>()
    val sideEffects: Flow<SetupSideEffect> = _sideEffects.receiveAsFlow()

    fun setAdbEnabled(enabled: Boolean) {
        developerSettings.isAdbEnabled = enabled
    }

    fun grantPermissions(permissions: List<String>) {
        viewModelScope.launch {
            if (!developerSettings.isAdbEnabled) {
                _sideEffects.send(SetupSideEffect.AdbRequired)
                return@launch
            }

            _state.value = SetupState.Processing

            try {
                permissionGranter.grantPermissions(permissions)
                _sideEffects.send(SetupSideEffect.PermissionsGranted)
            } catch (e: CancellationException) {
                throw e
            } catch (e: TimeoutException) {
                _sideEffects.send(SetupSideEffect.GrantPermissionsTimeout(permissions))
            } catch (e: IOException) {
                _sideEffects.send(SetupSideEffect.Error("ADB error: ${e.message}"))
            } catch (e: Exception) {
                _sideEffects.send(SetupSideEffect.Error(e.message ?: "Unknown error"))
            } finally {
                _state.value = SetupState.Idle
            }
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            permissionGranter: PermissionGranter, developerSettings: DeveloperSettings
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SetupViewModel(permissionGranter, developerSettings) as T
        }
    }
}
