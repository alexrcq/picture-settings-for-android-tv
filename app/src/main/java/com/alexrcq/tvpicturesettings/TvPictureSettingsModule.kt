package com.alexrcq.tvpicturesettings

import android.content.Context
import com.alexrcq.tvpicturesettings.storage.P1PictureSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettingsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TvPictureSettingsModule {

    @Singleton
    @Provides
    fun providePictureSettings(@ApplicationContext context: Context): PictureSettings {
        if (isCurrentTvModelP1Croods) {
            return P1PictureSettings(context)
        }
        return PictureSettingsImpl(context)
    }
}