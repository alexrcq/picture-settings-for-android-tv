package com.alexrcq.tvpicturesettings.ui.fragment.main

sealed interface MainUiState {
    object Idle : MainUiState
    object Loading : MainUiState
    object ScreenCapturing : MainUiState
    data class ScreenCaptureFinished(val isSuccess: Boolean) : MainUiState
}

sealed interface MainIntent {
    object CaptureScreenshot : MainIntent
    object EnableDarkModeAndToggleFilter : MainIntent
    object ToggleDarkMode : MainIntent
    object ToggleScreenPower : MainIntent
    class GrantPermissions(val permissions: List<String>) : MainIntent
    class ChangeBacklight(val value: Int) : MainIntent
}

sealed interface MainSideEffect {
    object ShowAdbRequired : MainSideEffect
    class ShowGrantPermissions(val permissions: List<String>) : MainSideEffect
    object ShowPermissionsGranted : MainSideEffect
    class ShowError(val errorMessage: String) : MainSideEffect
}