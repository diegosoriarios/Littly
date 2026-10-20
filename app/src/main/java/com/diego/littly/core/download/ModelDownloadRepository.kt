package com.diego.littly.core.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.diego.littly.core.common.di.ApplicationScope
import com.diego.littly.core.common.di.IoDispatcher
import com.diego.littly.core.llm.ModelLocator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
    private val modelLocator: ModelLocator
) {
    private val workManager = WorkManager.getInstance(context)

    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
    val downloadState: StateFlow<ModelDownloadState> = _downloadState.asStateFlow()

    init {
        observeWorkManager()
    }

    private fun observeWorkManager() {
        scope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(ModelDownloadWorker.WORK_NAME).collect { workInfos ->
                val workInfo = workInfos.firstOrNull()
                _downloadState.value = when (workInfo?.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt("progress", 0)
                        val bytesDownloaded = workInfo.progress.getLong("bytes_downloaded", 0)
                        val totalBytes = workInfo.progress.getLong("total_bytes", 0).takeIf { it > 0 }
                        if (totalBytes != null) {
                            ModelDownloadState.Downloading(bytesDownloaded, totalBytes)
                        } else {
                            ModelDownloadState.Downloading(bytesDownloaded, null)
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> ModelDownloadState.Completed
                    WorkInfo.State.FAILED -> ModelDownloadState.Failed("Download failed")
                    WorkInfo.State.CANCELLED -> ModelDownloadState.Idle
                    WorkInfo.State.BLOCKED -> ModelDownloadState.WaitingForNetwork
                    null -> if (modelLocator.isModelPresent()) {
                        ModelDownloadState.Completed
                    } else {
                        ModelDownloadState.Idle
                    }
                }
            }
        }
    }

    suspend fun startDownload(modelUrl: String = DEFAULT_MODEL_URL): Result<Unit> = withContext(ioDispatcher) {
        if (modelLocator.isModelPresent()) {
            return@withContext Result.success(Unit)
        }

        val destinationPath = modelLocator.modelFile.absolutePath
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(false)
            .build()

        val inputData = Data.Builder()
            .putString(ModelDownloadWorker.KEY_MODEL_URL, modelUrl)
            .putString(ModelDownloadWorker.KEY_DESTINATION_PATH, destinationPath)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            ModelDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        Result.success(Unit)
    }

    suspend fun cancelDownload() = withContext(ioDispatcher) {
        workManager.cancelUniqueWork(ModelDownloadWorker.WORK_NAME)
        _downloadState.value = ModelDownloadState.Idle
    }

    suspend fun deleteModel(): Result<Unit> = withContext(ioDispatcher) {
        try {
            cancelDownload()
            val file = modelLocator.modelFile
            if (file.exists()) {
                file.delete()
            }
            _downloadState.value = ModelDownloadState.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isModelPresent(): Boolean = modelLocator.isModelPresent()

    fun getModelSize(): Long {
        val file = modelLocator.modelFile
        return if (file.exists()) file.length() else 0
    }

    companion object {
        const val DEFAULT_MODEL_URL = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task"
    }
}

sealed class ModelDownloadState {
    data object Idle : ModelDownloadState()
    data object WaitingForNetwork : ModelDownloadState()
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long?) : ModelDownloadState()
    data object Completed : ModelDownloadState()
    data class Failed(val error: String) : ModelDownloadState()
}
