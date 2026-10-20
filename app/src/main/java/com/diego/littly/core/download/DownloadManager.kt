package com.diego.littly.core.download

import kotlinx.coroutines.flow.Flow

interface DownloadManager {
    fun observeProgress(): Flow<DownloadProgress>
    suspend fun startDownload(url: String, destination: String): Result<Unit>
    suspend fun cancelDownload()
    suspend fun deleteModel(): Result<Unit>
}

sealed class DownloadProgress {
    data object Idle : DownloadProgress()
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long?) : DownloadProgress()
    data class Completed(val filePath: String) : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
    data object Cancelled : DownloadProgress()
}
