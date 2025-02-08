package com.alexrcq.tvpicturesettings.domain

import android.provider.Settings
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.util.toBoolean
import kotlinx.coroutines.flow.Flow

interface TvSettings {
    val global: GlobalSettings
    val picture: PictureSettings

    var isScreenOn: Boolean

    fun tvSourceFlow(): Flow<String?>

    fun isAdbEnabled(): Boolean = global.getInt(Settings.Global.ADB_ENABLED).toBoolean()

    fun toggleScreenPower() {
        isScreenOn = !isScreenOn
    }
}