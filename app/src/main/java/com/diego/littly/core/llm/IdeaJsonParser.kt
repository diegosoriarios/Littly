package com.diego.littly.core.llm

import com.diego.littly.core.domain.model.GeneratedIdea
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object IdeaJsonParser {

    @Serializable
    private data class IdeaDto(
        val title: String? = null,
        val idea: String? = null,
        val text: String? = null,
        val description: String? = null
    )

    @Serializable
    private data class IdeasPayload(val ideas: List<IdeaDto> = emptyList())

    private val lenientJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val jsonObjectRegex = Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL)
    private val titleBodyPairRegex = Regex(
        "\"title\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"(?:idea|text|description|body)\"\\s*:\\s*\"([^\"]*)\"",
        RegexOption.DOT_MATCHES_ALL
    )
    private val bodyOnlyRegex = Regex(
        "\"(?:idea|text|description|body)\"\\s*:\\s*\"([^\"]*)\"",
        RegexOption.DOT_MATCHES_ALL
    )

    fun parse(raw: String, maxIdeas: Int = 8): List<GeneratedIdea> {
        if (raw.isBlank()) return emptyList()
        val cleaned = stripFences(raw)

        decodePayload(cleaned)?.let { return it.take(maxIdeas) }

        jsonObjectRegex.find(cleaned)?.let { match ->
            decodePayload(match.value)?.let { return it.take(maxIdeas) }
        }

        salvagePairs(cleaned)?.let { return it.take(maxIdeas) }
        salvageBodies(cleaned)?.let { return it.take(maxIdeas) }

        return emptyList()
    }

    private fun stripFences(raw: String): String = raw
        .replace("```json", "", ignoreCase = true)
        .replace("```", "")
        .trim()

    private fun decodePayload(text: String): List<GeneratedIdea>? = try {
        val payload = lenientJson.decodeFromString<IdeasPayload>(text)
        val ideas = payload.ideas
            .mapNotNull { dto ->
                val body = (dto.idea ?: dto.text ?: dto.description)?.clean()
                if (body.isNullOrBlank()) null else GeneratedIdea(title = dto.title?.clean(), text = body)
            }
            .filter { it.text.isNotBlank() }
        if (ideas.isEmpty()) null else ideas
    } catch (_: Exception) {
        null
    }

    private fun salvagePairs(text: String): List<GeneratedIdea>? {
        val matches = titleBodyPairRegex.findAll(text).toList()
        if (matches.isEmpty()) return null
        val ideas = matches.mapNotNull { match ->
            val body = match.groupValues[2].clean()
            if (body.isBlank()) null else GeneratedIdea(title = match.groupValues[1].clean(), text = body)
        }
        return ideas.ifEmpty { null }
    }

    private fun salvageBodies(text: String): List<GeneratedIdea>? {
        val matches = bodyOnlyRegex.findAll(text).toList()
        if (matches.isEmpty()) return null
        val ideas = matches.mapNotNull { match ->
            val body = match.groupValues[1].clean()
            if (body.isBlank()) null else GeneratedIdea(title = null, text = body)
        }
        return ideas.ifEmpty { null }
    }

    private fun String.clean(): String = this
        .replace("\\n", " ")
        .replace("\\\"", "\"")
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim('"', '\'', ',')
        .let { if (it.isBlank()) "" else it.replaceFirstChar { c -> c.lowercaseChar() } }
        .let { if (it.isEmpty()) it else it.replaceFirstChar { c -> c.uppercaseChar() } }
}
