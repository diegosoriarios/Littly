package com.diego.littly.di

import android.content.Context
import androidx.work.WorkManager
import com.diego.littly.core.download.DownloadManager
import com.diego.littly.core.download.OkHttpDownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideDownloadManager(downloadManager: OkHttpDownloadManager): DownloadManager =
        downloadManager
}
