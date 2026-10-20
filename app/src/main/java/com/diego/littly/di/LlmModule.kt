package com.diego.littly.di

import com.diego.littly.core.llm.FallbackIdeaGenerator
import com.diego.littly.core.llm.IdeaGenerator
import com.diego.littly.core.llm.MediaPipeIdeaGenerator
import com.diego.littly.core.llm.ModelLocator
import com.diego.littly.core.llm.NoopIdeaGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {

    @Provides
    @Singleton
    fun provideIdeaGenerator(
        mediaPipeIdeaGenerator: MediaPipeIdeaGenerator,
        noopIdeaGenerator: NoopIdeaGenerator,
        modelLocator: ModelLocator
    ): IdeaGenerator = FallbackIdeaGenerator(
        primaryProvider = {
            if (modelLocator.isModelPresent()) mediaPipeIdeaGenerator else null
        },
        fallback = noopIdeaGenerator
    )
}
