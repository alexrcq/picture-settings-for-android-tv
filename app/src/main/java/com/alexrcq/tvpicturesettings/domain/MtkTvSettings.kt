package com.alexrcq.tvpicturesettings.domain

import android.content.ContentResolver
import android.net.Uri
import com.alexrcq.tvpicturesettings.storage.GlobalSettings
import com.alexrcq.tvpicturesettings.storage.MtkGlobalKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

private const val TV_SOURCE_NAME_URI =
    "content://com.mediatek.tv.internal.data/global_value/multi_view_main_source_name"
private const val COLUMN_VALUE = "value"
private const val TV_SOURCE_INACTIVE = "Null"
private const val TV_SOURCE_CHECK_INTERVAL = 2500L

class MtkTvSettings(
    private val contentResolver: ContentResolver,
    override val global: GlobalSettings,
    override val picture: PictureSettings
) : TvSettings {

    override fun tvSourceFlow(): Flow<String?> = flow {
        // the content provider doesn't implement notifyChange(), so we're polling
        while (true) {
            emit(getActiveTvSourceName())
            delay(TV_SOURCE_CHECK_INTERVAL)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    private fun getActiveTvSourceName(): String? {
        val currentSourceNameUri = Uri.parse(TV_SOURCE_NAME_URI)
        val cursor = contentResolver.query(
            currentSourceNameUri, null, null, null
        ) ?: return null
        cursor.moveToFirst()
        val currentSourceName = try {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE))
        } catch (e: Exception) {
            return null
        } finally {
            cursor.close()
        }
        Timber.d("currentSource: $currentSourceName")
        return if (currentSourceName == TV_SOURCE_INACTIVE) null else currentSourceName
    }

    private var isScreenPowerOn: Boolean by global.booleanSetting(MtkGlobalKeys.POWER_PICTURE_OFF)

    override fun toggleScreenPower() {
        isScreenPowerOn = !isScreenPowerOn
    }
}