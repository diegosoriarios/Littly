package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.HabitCategory

object IdeaPrompts {

    fun build(request: IdeaRequest): String {
        val sb = StringBuilder()
        sb.appendLine(
            "You are Littly, a warm and encouraging habit companion. " +
                "The user is building the habit: \"${request.habitName.trim()}\"."
        )
        sb.appendLine("Habit category: ${request.category.name.lowercase()}.")
        sb.appendLine(categoryGuidance(request.category))
        sb.appendLine()
        sb.appendLine(
            "Suggest exactly ${request.count} fresh ideas for a single session today. " +
                "Each idea must be specific and doable in 5 to 30 minutes. " +
                "Each has a short title (max 4 words) and one sentence description (max 20 words). " +
                "Tone: warm, playful, motivating."
        )
        sb.appendLine()
        sb.appendLine(
            "Respond with ONLY valid JSON, no markdown fences, no commentary, " +
                "in exactly this format:"
        )
        sb.appendLine("{\"ideas\":[{\"title\":\"...\",\"idea\":\"...\"}]}")
        if (request.recentIdeas.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Do not repeat or paraphrase these previous ideas:")
            request.recentIdeas.forEach { recent ->
                sb.appendLine("- $recent")
            }
        }
        return sb.toString()
    }

    private fun categoryGuidance(category: HabitCategory): String = when (category) {
        HabitCategory.CREATIVE ->
            "Every idea should spark imagination: suggest a subject, a twist, a constraint, or a medium to try."
        HabitCategory.READING ->
            "Suggest specific things to read: a genre, topic, author, format, or a place to discover something new."
        HabitCategory.LEARNING ->
            "Suggest micro-lessons, practice drills, memory tricks, or small topics to explore."
        HabitCategory.FITNESS ->
            "Suggest concrete movement ideas doable at home or outdoors, with a playful angle."
        HabitCategory.MINDFULNESS ->
            "Suggest calming rituals, short reflections, breathing exercises, or moments of gratitude."
        HabitCategory.SOCIAL ->
            "Suggest small, low-pressure ways to connect with people."
        HabitCategory.PRODUCTIVITY ->
            "Suggest small organizing, planning, or tidying wins that give quick satisfaction."
        HabitCategory.GENERIC ->
            "Suggest simple, concrete actions that make the habit feel rewarding."
    }
}
