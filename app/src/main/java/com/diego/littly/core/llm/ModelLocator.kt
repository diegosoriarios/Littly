package com.diego.littly.core.llm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelLocator @Inject constructor(
    @ApplicationContext context: Context
) {
    val modelsDir: File = File(context.filesDir, "models")

    val modelFile: File
        get() = File(modelsDir, MODEL_FILE_NAME)

    fun isModelPresent(): Boolean =
        modelFile.exists() && modelFile.length() >= MIN_MODEL_BYTES

    companion object {
        const val MODEL_FILE_NAME = "gemma-3-1b-it-int4.task"

        const val MODEL_ID = "gemma-3-1b-it-int4"

        const val MIN_MODEL_BYTES = 100L * 1024 * 1024
    }
}
