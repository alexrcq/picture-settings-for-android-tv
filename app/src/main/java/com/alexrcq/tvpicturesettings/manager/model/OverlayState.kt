package com.alexrcq.tvpicturesettings.manager.model

data class OverlayState(
    val shouldShowDimmer: Boolean,
    val dimmingPower: Int,
    val shouldShowBlueLightFilter: Boolean,
    val blueLightFilterTemp: Int,
    val blueLightFilterPower: Int
)