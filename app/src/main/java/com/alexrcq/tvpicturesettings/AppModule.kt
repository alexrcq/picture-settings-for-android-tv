package com.alexrcq.tvpicturesettings

import android.content.Context
import com.alexrcq.tvpicturesettings.adblib.AdbShell
import com.alexrcq.tvpicturesettings.storage.PictureSettings
import com.alexrcq.tvpicturesettings.storage.PictureSettingsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Singleton
    @Binds
    abstract fun bindPictureSettings(pictureSettings: PictureSettingsImpl): PictureSettings

    companion object {
        @Singleton
        @Provides
        fun provideAdbShell(@ApplicationContext context: Context): AdbShell {
            return AdbShell(context)
        }
    }
}