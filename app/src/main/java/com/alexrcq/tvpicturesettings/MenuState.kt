package com.alexrcq.tvpicturesettings

sealed class MenuState {
    object Idle : MenuState()
    object Loading : MenuState()
    object ScreenCapturing : MenuState()
    data class ScreenCaptureFinished(val isSuccess: Boolean) : MenuState()
}

sealed class MenuIntent {
    class ChangeMenuState(val menuState: MenuState) : MenuIntent()
    object CaptureScreenshot : MenuIntent()
}
