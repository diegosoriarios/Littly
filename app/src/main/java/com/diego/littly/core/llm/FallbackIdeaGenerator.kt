package com.diego.littly.core.llm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

class FallbackIdeaGenerator(
    private val primaryProvider: () -> IdeaGenerator?,
    private val fallback: IdeaGenerator
) : IdeaGenerator {

    override fun generate(request: IdeaRequest): Flow<IdeaEvent> = flow {
        val primary = primaryProvider()
        if (primary == null) {
            fallback.generate(request).collect { emit(it) }
            return@flow
        }
        
        var primaryFailed = false
        
        primary.generate(request).collect { event ->
            if (event is IdeaEvent.Failed) {
                primaryFailed = true
            } else {
                emit(event)
            }
        }
        
        if (primaryFailed) {
            fallback.generate(request).collect { emit(it) }
        }
    }
}
