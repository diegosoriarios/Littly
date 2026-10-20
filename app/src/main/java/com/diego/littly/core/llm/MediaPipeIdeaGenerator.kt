package com.diego.littly.core.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class MediaPipeIdeaGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelLocator: ModelLocator
) : IdeaGenerator {

    private val lock = Any()

    @Volatile
    private var inference: LlmInference? = null

    override fun generate(request: IdeaRequest): Flow<IdeaEvent> = callbackFlow {
        if (!modelLocator.isModelPresent()) {
            trySend(IdeaEvent.Failed("model not downloaded"))
            close()
            awaitClose { }
            return@callbackFlow
        }
        val engine = try {
            obtainInference()
        } catch (t: Throwable) {
            trySend(IdeaEvent.Failed("failed to load model: ${t.message}"))
            close()
            awaitClose { }
            return@callbackFlow
        }
        val prompt = IdeaPrompts.build(request)
        try {
            engine.generateResponseAsync(prompt) { partial, done ->
                if (done) {
                    val ideas = IdeaJsonParser.parse(partial)
                    if (ideas.isEmpty()) {
                        trySend(IdeaEvent.Failed("model returned no parseable ideas"))
                    } else {
                        trySend(IdeaEvent.Completed(ideas, ModelLocator.MODEL_ID))
                    }
                    close()
                } else {
                    trySend(IdeaEvent.Partial(partial))
                }
            }
        } catch (t: Throwable) {
            trySend(IdeaEvent.Failed("generation failed: ${t.message}"))
            close()
        }
        awaitClose { }
    }

    private fun obtainInference(): LlmInference = synchronized(lock) {
        inference ?: createInference().also { inference = it }
    }

    private fun createInference(): LlmInference {
        val modelPath = modelLocator.modelFile.absolutePath
        return try {
            createWithBackend(modelPath, LlmInference.Backend.GPU)
        } catch (gpuError: Throwable) {
            createWithBackend(modelPath, LlmInference.Backend.CPU)
        }
    }

    private fun createWithBackend(modelPath: String, backend: LlmInference.Backend): LlmInference {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(MAX_TOKENS)
            .setPreferredBackend(backend)
            .build()
        return LlmInference.createFromOptions(context, options)
    }

    fun close() {
        synchronized(lock) {
            inference?.close()
            inference = null
        }
    }

    private companion object {
        const val MAX_TOKENS = 384
    }
}
