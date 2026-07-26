package com.alexrcq.tvpicturesettings.manager.model

enum class BlueLightFilterMode(val value: String) {
    OFF("0"),
    NIGHT_ONLY("1"),
    ALWAYS("2");

    companion object {
        fun of(value: String?): BlueLightFilterMode = entries.find { it.value == value } ?: OFF
    }
}