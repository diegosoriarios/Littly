package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.GeneratedIdea
import com.diego.littly.core.domain.model.HabitCategory
import kotlinx.coroutines.flow.Flow

sealed interface LlmState {
    data object NotDownloaded : LlmState
    data class Downloading(val progress: Float) : LlmState
    data object Ready : LlmState
    data object Unsupported : LlmState
    data class Error(val message: String) : LlmState
}

data class IdeaRequest(
    val habitName: String,
    val category: HabitCategory,
    val recentIdeas: List<String> = emptyList(),
    val count: Int = 3
)

sealed interface IdeaEvent {
    data class Partial(val textSoFar: String) : IdeaEvent
    data class Completed(val ideas: List<GeneratedIdea>, val model: String) : IdeaEvent
    data class Failed(val reason: String) : IdeaEvent
}

interface IdeaGenerator {
    fun generate(request: IdeaRequest): Flow<IdeaEvent>
}
