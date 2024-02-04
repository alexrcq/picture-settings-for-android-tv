package com.alexrcq.tvpicturesettings.ui.fragment.main

sealed interface MainState {
    object Idle : MainState
    object Loading : MainState
    object ScreenCapturing : MainState
    data class ScreenCaptureFinished(val isSuccess: Boolean) : MainState
}

sealed interface MainIntent {
    object CaptureScreenshot : MainIntent
    object EnableDarkModeAndToggleFilter : MainIntent
    object ToggleDarkMode : MainIntent
    object ToggleScreenPower : MainIntent
    class GrantPermission(val permission: String): MainIntent
    class ChangeBacklight(val value: Int): MainIntent
}

sealed interface MainSideEffect {
    object ShowAdbRequired : MainSideEffect
    object ShowAcceptAdbForPermissions : MainSideEffect
    class ShowError(val message: String) : MainSideEffect
}