package com.diego.littly.di

import android.content.Context
import androidx.room.Room
import com.diego.littly.core.database.LittlyDatabase
import com.diego.littly.core.database.dao.HabitDao
import com.diego.littly.core.database.dao.HabitLogDao
import com.diego.littly.core.database.dao.IdeaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LittlyDatabase =
        Room.databaseBuilder(context, LittlyDatabase::class.java, LittlyDatabase.NAME)
            .addMigrations(LittlyDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideHabitDao(database: LittlyDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitLogDao(database: LittlyDatabase): HabitLogDao = database.habitLogDao()

    @Provides
    fun provideIdeaDao(database: LittlyDatabase): IdeaDao = database.ideaDao()
}
