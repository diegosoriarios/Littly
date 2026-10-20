package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.GeneratedIdea
import com.diego.littly.core.domain.model.HabitCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackIdeaGeneratorTest {

    private val request = IdeaRequest("draw more", HabitCategory.CREATIVE, emptyList(), count = 3)

    private val curated = listOf(
        IdeaEvent.Partial("thinking"),
        IdeaEvent.Completed(listOf(GeneratedIdea(null, "curated idea")), "curated")
    )

    @Test
    fun streamsPrimaryEventsWhenHealthy() = runTest {
        val primaryEvents = listOf(
            IdeaEvent.Partial("{"),
            IdeaEvent.Partial("{\"ideas\":"),
            IdeaEvent.Completed(listOf(GeneratedIdea("T", "model idea")), "gemma")
        )
        val fallbackInvocations = mutableListOf<IdeaRequest>()
        val fallback = FakeRecordingGenerator(fallbackInvocations, curated)
        val generator = FallbackIdeaGenerator(
            primaryProvider = { FakeIdeaGenerator(primaryEvents) },
            fallback = fallback
        )

        val events = collect(generator)

        assertEquals(primaryEvents, events)
        assertTrue(fallbackInvocations.isEmpty())
    }

    @Test
    fun fallsBackWhenPrimaryFails() = runTest {
        val primaryEvents = listOf(
            IdeaEvent.Partial("garbage"),
            IdeaEvent.Failed("unparseable")
        )
        val fallbackInvocations = mutableListOf<IdeaRequest>()
        val fallback = FakeRecordingGenerator(fallbackInvocations, curated)
        val generator = FallbackIdeaGenerator(
            primaryProvider = { FakeIdeaGenerator(primaryEvents) },
            fallback = fallback
        )

        val events = collect(generator)

        assertTrue(events.none { it is IdeaEvent.Failed })
        assertEquals(3, events.size)
        assertTrue(events[0] is IdeaEvent.Partial)
        assertTrue(events[1] is IdeaEvent.Partial)
        assertTrue(events[2] is IdeaEvent.Completed)
        assertEquals(listOf(request), fallbackInvocations)
    }

    @Test
    fun usesFallbackDirectlyWhenNoPrimary() = runTest {
        val fallbackInvocations = mutableListOf<IdeaRequest>()
        val fallback = FakeRecordingGenerator(fallbackInvocations, curated)
        val generator = FallbackIdeaGenerator(
            primaryProvider = { null },
            fallback = fallback
        )

        val events = collect(generator)

        assertEquals(curated, events)
        assertEquals(listOf(request), fallbackInvocations)
    }

    @Test
    fun primaryFailureAfterPartialsStillFallsBack() = runTest {
        val primaryEvents = listOf(
            IdeaEvent.Partial("partial one"),
            IdeaEvent.Partial("partial two"),
            IdeaEvent.Failed("oom")
        )
        val generator = FallbackIdeaGenerator(
            primaryProvider = { FakeIdeaGenerator(primaryEvents) },
            fallback = FakeIdeaGenerator(curated)
        )

        val events = collect(generator)

        assertTrue(events[0] is IdeaEvent.Partial)
        assertTrue(events[1] is IdeaEvent.Partial)
        assertTrue(events.last() is IdeaEvent.Completed)
        assertEquals(4, events.size)
    }

    private suspend fun collect(generator: IdeaGenerator): List<IdeaEvent> {
        val list = mutableListOf<IdeaEvent>()
        generator.generate(request).collect { list.add(it) }
        return list
    }

    private class FakeRecordingGenerator(
        val invocations: MutableList<IdeaRequest>,
        private val events: List<IdeaEvent>
    ) : IdeaGenerator {
        override fun generate(request: IdeaRequest) = flow {
            invocations.add(request)
            events.forEach { emit(it) }
        }
    }

    private class FakeIdeaGenerator(
        private val events: List<IdeaEvent>
    ) : IdeaGenerator {
        override fun generate(request: IdeaRequest) = flow {
            events.forEach { emit(it) }
        }
    }
}
