package com.alexrcq.tvpicturesettings

import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettingsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TvPictureSettingsModule {

    @Singleton
    @Binds
    abstract fun providePictureSettings(pictureSettings: PictureSettingsImpl): PictureSettings
}