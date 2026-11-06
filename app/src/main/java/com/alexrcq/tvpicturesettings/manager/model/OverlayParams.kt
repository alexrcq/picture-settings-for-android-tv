package com.alexrcq.tvpicturesettings.manager.model

data class OverlayParams(
    val isDimmingEnabled: Boolean,
    val dimmingPower: Int,
    val blueLightFilterMode: BlueLightFilterMode,
    val blueLightFilterTemp: Int,
    val blueLightFilterPower: Int
)