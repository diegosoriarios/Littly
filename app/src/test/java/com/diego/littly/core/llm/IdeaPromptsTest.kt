package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdeaPromptsTest {

    @Test
    fun includesHabitNameAndCategory() {
        val prompt = IdeaPrompts.build(
            IdeaRequest(
                habitName = "draw more",
                category = HabitCategory.CREATIVE,
                count = 3
            )
        )
        assertTrue(prompt.contains("\"draw more\""))
        assertTrue(prompt.contains("creative"))
    }

    @Test
    fun requestsExactNumberOfIdeas() {
        val prompt = IdeaPrompts.build(
            IdeaRequest(habitName = "read", category = HabitCategory.READING, count = 5)
        )
        assertTrue(prompt.contains("exactly 5"))
    }

    @Test
    fun demandsJsonSchema() {
        val prompt = IdeaPrompts.build(
            IdeaRequest(habitName = "run", category = HabitCategory.FITNESS)
        )
        assertTrue(prompt.contains("{\"ideas\":[{\"title\":\"...\",\"idea\":\"...\"}]}"))
        assertTrue(prompt.contains("ONLY valid JSON"))
    }

    @Test
    fun listsRecentIdeasForAntiRepeat() {
        val prompt = IdeaPrompts.build(
            IdeaRequest(
                habitName = "draw more",
                category = HabitCategory.CREATIVE,
                recentIdeas = listOf("Ink garden", "Blind portrait"),
                count = 3
            )
        )
        assertTrue(prompt.contains("Do not repeat"))
        assertTrue(prompt.contains("- Ink garden"))
        assertTrue(prompt.contains("- Blind portrait"))
    }

    @Test
    fun omitsRecentSectionWhenEmpty() {
        val prompt = IdeaPrompts.build(
            IdeaRequest(habitName = "draw more", category = HabitCategory.CREATIVE)
        )
        assertFalse(prompt.contains("Do not repeat"))
    }

    @Test
    fun categoryGuidanceDiffers() {
        val creative = IdeaPrompts.build(
            IdeaRequest(habitName = "draw", category = HabitCategory.CREATIVE)
        )
        val fitness = IdeaPrompts.build(
            IdeaRequest(habitName = "run", category = HabitCategory.FITNESS)
        )
        assertTrue(creative.contains("spark imagination"))
        assertTrue(fitness.contains("movement"))
        assertFalse(creative == fitness)
    }

    @Test
    fun trimsHabitName() {
        val prompt = IdeaPrompts.build(
            IdeaRequest(habitName = "  read  ", category = HabitCategory.READING)
        )
        assertTrue(prompt.contains("\"read\""))
    }
}
