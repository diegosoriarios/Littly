package com.diego.littly.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.diego.littly.core.data.repository.HabitRepositoryImpl
import com.diego.littly.core.data.repository.IdeaRepositoryImpl
import com.diego.littly.core.data.settings.DataStoreSettingsRepository
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.time.SystemTimeProvider
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.domain.usecase.IdeaQueueFiller
import com.diego.littly.core.domain.usecase.IdeaQueueFillerImpl
import com.diego.littly.core.notifications.AlarmScheduler
import com.diego.littly.core.notifications.AndroidAlarmScheduler
import com.diego.littly.core.notifications.ReminderPlanner
import com.diego.littly.core.notifications.ReminderPlannerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { File(context.filesDir, "datastore/littly_settings.preferences_pb") }
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    abstract fun bindIdeaRepository(impl: IdeaRepositoryImpl): IdeaRepository

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    abstract fun bindAlarmScheduler(impl: AndroidAlarmScheduler): AlarmScheduler

    @Binds
    abstract fun bindReminderPlanner(impl: ReminderPlannerImpl): ReminderPlanner

    @Binds
    abstract fun bindIdeaQueueFiller(impl: IdeaQueueFillerImpl): IdeaQueueFiller
}
