package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.HabitCategory

object HabitClassifier {

    private val keywords: List<Pair<HabitCategory, List<String>>> = listOf(
        HabitCategory.CREATIVE to listOf(
            "draw", "sketch", "paint", "doodle", "art", "illustrat", "design",
            "write", "poem", "story", "music", "guitar", "piano", "sing", "song",
            "craft", "knit", "crochet", "photo", "collage", "sculpt", "comic"
        ),
        HabitCategory.READING to listOf(
            "read", "book", "novel", "article", "library", "magazine",
            "poetry", "audiobook", "chapter"
        ),
        HabitCategory.LEARNING to listOf(
            "learn", "study", "language", "spanish", "french", "german", "japanese",
            "vocabulary", "vocab", "duolingo", "course", "lesson", "coding", "code",
            "programming", "math", "memoriz", "flashcard", "chess", "instrument"
        ),
        HabitCategory.FITNESS to listOf(
            "run", "jog", "gym", "walk", "yoga", "workout", "exercise", "stretch",
            "train", "swim", "cycle", "bike", "steps", "pushup", "squat", "plank",
            "hike", "weights", "cardio"
        ),
        HabitCategory.MINDFULNESS to listOf(
            "meditat", "mindful", "breath", "gratitude", "grateful", "relax",
            "calm", "sleep", "journal", "reflect", "prayer", "yoga nidra", "unplug"
        ),
        HabitCategory.SOCIAL to listOf(
            "call", "friend", "family", "date night", "social", "talk", "meet",
            "letter", "connect", "community", "catch up"
        ),
        HabitCategory.PRODUCTIVITY to listOf(
            "clean", "tidy", "organiz", "declutter", "inbox", "budget", "financ",
            "schedule", "routine", "wake", "morning", "admin", "chores", "planning"
        )
    )

    fun classify(habitName: String): HabitCategory {
        val normalized = habitName.lowercase()
        val words = normalized.split(Regex("""[\s,.;!?]+""")).filter { it.isNotEmpty() }
        
        // First pass: exact word matches (highest priority)
        keywords.forEach { (category, keywords) ->
            if (keywords.any { keyword -> 
                words.any { word -> word == keyword }
            }) return category
        }
        
        // Second pass: prefix matches (lower priority)
        keywords.forEach { (category, keywords) ->
            if (keywords.any { keyword -> 
                words.any { word -> word.startsWith(keyword) && word.length > keyword.length }
            }) return category
        }
        
        return HabitCategory.GENERIC
    }
}
