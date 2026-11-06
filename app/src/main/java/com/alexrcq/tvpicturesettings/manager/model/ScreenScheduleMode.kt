package com.alexrcq.tvpicturesettings.manager.model

enum class ScreenScheduleMode(val value: String) {
    OFF("0"),
    DAILY("1"),
    WEEKLY("2");

    companion object {
        fun of(value: String?): ScreenScheduleMode = entries.find { it.value == value } ?: OFF
    }
}
