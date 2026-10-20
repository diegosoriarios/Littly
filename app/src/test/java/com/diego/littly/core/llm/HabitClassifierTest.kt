package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitClassifierTest {

    @Test
    fun classifiesCreativeHabits() {
        assertEquals(HabitCategory.CREATIVE, HabitClassifier.classify("draw more"))
        assertEquals(HabitCategory.CREATIVE, HabitClassifier.classify("practice guitar"))
        assertEquals(HabitCategory.CREATIVE, HabitClassifier.classify("Write a story"))
    }

    @Test
    fun classifiesReadingHabits() {
        assertEquals(HabitCategory.READING, HabitClassifier.classify("read 20 pages"))
        assertEquals(HabitCategory.READING, HabitClassifier.classify("finish one novel a month"))
        assertEquals(HabitCategory.READING, HabitClassifier.classify("read articles at breakfast"))
    }

    @Test
    fun classifiesLearningHabits() {
        assertEquals(HabitCategory.LEARNING, HabitClassifier.classify("learn Spanish"))
        assertEquals(HabitCategory.LEARNING, HabitClassifier.classify("practice coding"))
        assertEquals(HabitCategory.LEARNING, HabitClassifier.classify("10 min of vocabulary"))
    }

    @Test
    fun classifiesFitnessHabits() {
        assertEquals(HabitCategory.FITNESS, HabitClassifier.classify("run every morning"))
        assertEquals(HabitCategory.FITNESS, HabitClassifier.classify("go to the gym"))
        assertEquals(HabitCategory.FITNESS, HabitClassifier.classify("evening yoga"))
    }

    @Test
    fun classifiesMindfulnessHabits() {
        assertEquals(HabitCategory.MINDFULNESS, HabitClassifier.classify("meditate 10 minutes"))
        assertEquals(HabitCategory.MINDFULNESS, HabitClassifier.classify("gratitude journal"))
        assertEquals(HabitCategory.MINDFULNESS, HabitClassifier.classify("breathe deeply"))
    }

    @Test
    fun classifiesSocialHabits() {
        assertEquals(HabitCategory.SOCIAL, HabitClassifier.classify("call mom weekly"))
        assertEquals(HabitCategory.SOCIAL, HabitClassifier.classify("meet a friend for coffee"))
    }

    @Test
    fun classifiesProductivityHabits() {
        assertEquals(HabitCategory.PRODUCTIVITY, HabitClassifier.classify("tidy the desk"))
        assertEquals(HabitCategory.PRODUCTIVITY, HabitClassifier.classify("check the budget"))
        assertEquals(HabitCategory.PRODUCTIVITY, HabitClassifier.classify("clean inbox to zero"))
    }

    @Test
    fun fallsBackToGeneric() {
        assertEquals(HabitCategory.GENERIC, HabitClassifier.classify("water the plants"))
        assertEquals(HabitCategory.GENERIC, HabitClassifier.classify(""))
        assertEquals(HabitCategory.GENERIC, HabitClassifier.classify("xyzzy"))
    }

    @Test
    fun isCaseInsensitive() {
        assertEquals(HabitCategory.FITNESS, HabitClassifier.classify("RUN DAILY"))
    }
}
