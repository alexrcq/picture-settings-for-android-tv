package com.alexrcq.tvpicturesettings.platform.mtk

import android.content.ContentResolver
import androidx.core.net.toUri
import com.alexrcq.tvpicturesettings.platform.TvInputMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MtkTvInputMonitor(private val contentResolver: ContentResolver): TvInputMonitor {

    override val activeInput: Flow<String?> = flow {
        // The content provider doesn't implement notifyChange(), so we're polling
        while (true) {
            emit(getActiveInputName())
            delay(INPUT_CHECK_INTERVAL)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    private val inputUri = INPUT_URI_STRING.toUri()

    private fun getActiveInputName(): String? = try {
        contentResolver.query(inputUri, null, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALUE))
            if (name == INPUT_NONE) null else name
        }
    } catch (e: Exception) {
        null
    }

    companion object {
        private const val INPUT_URI_STRING =
            "content://com.mediatek.tv.internal.data/global_value/multi_view_main_source_name"
        private const val COLUMN_VALUE = "value"
        private const val INPUT_NONE = "Null"
        private const val INPUT_CHECK_INTERVAL = 2500L
    }
}