package com.diego.littly.widget

import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.time.TimeProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habitRepository(): HabitRepository
    fun ideaRepository(): IdeaRepository
    fun settingsRepository(): SettingsRepository
    fun timeProvider(): TimeProvider
    fun widgetUpdateHelper(): WidgetUpdateHelper
}
