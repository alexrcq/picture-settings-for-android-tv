package com.alexrcq.tvpicturesettings.storage

import android.content.ContentResolver
import android.net.Uri
import timber.log.Timber

private const val TV_SOURCE_NAME_URI =
    "content://com.mediatek.tv.internal.data/global_value/multi_view_main_source_name"
private const val COLUMN_VALUE = "value"
private const val TV_SOURCE_INACTIVE = "Null"

class MtkTvSettings(
    private val contentResolver: ContentResolver,
    override val global: GlobalSettings,
    override val picture: TvSettings.Picture
) : TvSettings {

    override fun isTvSourceInactive(): Boolean {
        val currentSourceNameUri = Uri.parse(TV_SOURCE_NAME_URI)
        val cursor = contentResolver.query(
            currentSourceNameUri, null, null, null
        ) ?: return true
        cursor.moveToFirst()
        val currentSourceName = try {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE))
        } catch (e: Exception) {
            return true
        } finally {
            cursor.close()
        }
        Timber.d("currentSource: $currentSourceName")
        return currentSourceName == TV_SOURCE_INACTIVE
    }

    private var isScreenPowerOn: Boolean by global.booleanSetting(MtkGlobalKeys.POWER_PICTURE_OFF)

    override fun toggleScreenPower() {
        isScreenPowerOn = !isScreenPowerOn
    }
}