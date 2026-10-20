package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.llm.IdeaEvent
import com.diego.littly.core.llm.IdeaGenerator
import com.diego.littly.core.llm.IdeaRequest
import com.diego.littly.core.llm.NoopIdeaGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoopIdeaGeneratorTest {

    private val generator = NoopIdeaGenerator()

    @Test
    fun completesWithCuratedIdeas() = runTest {
        val event = generator.generate(
            IdeaRequest("draw more", HabitCategory.CREATIVE, emptyList(), count = 3)
        ).first()

        assertTrue(event is IdeaEvent.Completed)
        event as IdeaEvent.Completed
        assertEquals("curated", event.model)
        assertEquals(3, event.ideas.size)
    }

    @Test
    fun avoidsRecentIdeas() = runTest {
        val bank = listOf(
            "Draw a garden using only swirling ink lines, no shapes allowed.",
            "Sketch a whole city inside an everyday object like a teapot.",
            "Draw something familiar but swap two colors in a surprising way.",
            "Create a drawing without lifting the pen from the paper.",
            "Illustrate today's mood as if it were the weather.",
            "Draw a person or pet without looking at the paper once."
        )
        val event = generator.generate(
            IdeaRequest(
                habitName = "draw more",
                category = HabitCategory.CREATIVE,
                recentIdeas = bank,
                count = 3
            )
        ).first()

        event as IdeaEvent.Completed
        event.ideas.forEach { idea ->
            assertTrue(bank.none { recent -> recent.equals(idea.text, ignoreCase = true) })
        }
    }

    @Test
    fun fallsBackToGenericBankWhenCategoryExhausted() = runTest {
        val genericBank = listOf(
            "Set a timer and give the habit just five honest minutes.",
            "Pick the smallest possible win and bank it today.",
            "Share one line about your progress with someone who cheers you on.",
            "Do today's session somewhere you have never done it before.",
            "Attach the habit to something you already do every day.",
            "Spend a minute noticing how far you have already come."
        )
        val event = generator.generate(
            IdeaRequest(
                habitName = "draw more",
                category = HabitCategory.CREATIVE,
                recentIdeas = genericBank + listOf(
                    "Draw a garden using only swirling ink lines, no shapes allowed.",
                    "Sketch a whole city inside an everyday object like a teapot.",
                    "Draw something familiar but swap two colors in a surprising way.",
                    "Create a drawing without lifting the pen from the paper.",
                    "Illustrate today's mood as if it were the weather.",
                    "Draw a person or pet without looking at the paper once."
                ),
                count = 2
            )
        ).first()

        assertTrue(event is IdeaEvent.Completed)
        assertEquals(2, (event as IdeaEvent.Completed).ideas.size)
    }

    @Test
    fun respectsRequestedCount() = runTest {
        val event = generator.generate(
            IdeaRequest("read more", HabitCategory.READING, emptyList(), count = 5)
        ).first()
        assertEquals(5, (event as IdeaEvent.Completed).ideas.size)
    }
}
