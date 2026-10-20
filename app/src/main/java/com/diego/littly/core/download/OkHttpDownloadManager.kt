package com.diego.littly.core.download

import android.content.Context
import com.diego.littly.core.common.di.ApplicationScope
import com.diego.littly.core.common.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) : DownloadManager {

    private val client = OkHttpClient.Builder().build()
    private val _progress = MutableStateFlow<DownloadProgress>(DownloadProgress.Idle)
    private var currentJob: kotlinx.coroutines.Job? = null

    override fun observeProgress(): StateFlow<DownloadProgress> = _progress.asStateFlow()

    override suspend fun startDownload(url: String, destination: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                _progress.value = DownloadProgress.Downloading(0, null)
                val file = File(destination)
                file.parentFile?.mkdirs()

                val existingSize = if (file.exists()) file.length() else 0L

                val request = Request.Builder().apply {
                    url(url)
                    if (existingSize > 0) {
                        header("Range", "bytes=$existingSize-")
                    }
                }.build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful && response.code != 206) {
                    response.close()
                    val error = "HTTP ${response.code}: ${response.message}"
                    _progress.value = DownloadProgress.Failed(error)
                    return@withContext Result.failure(IOException(error))
                }

                val body = response.body ?: run {
                    _progress.value = DownloadProgress.Failed("Empty response body")
                    return@withContext Result.failure(IOException("Empty response body"))
                }

                val contentLength = body.contentLength()
                val totalBytes = if (response.code == 206) {
                    existingSize + contentLength
                } else {
                    contentLength
                }

                val append = response.code == 206
                val sink = file.sink(append).buffer()
                val source = body.source()

                var bytesDownloaded = existingSize
                val buffer = ByteArray(8192)

                while (true) {
                    val read = source.read(buffer, 0, buffer.size)
                    if (read == -1) break

                    sink.write(buffer, 0, read)
                    bytesDownloaded += read
                    _progress.value = DownloadProgress.Downloading(bytesDownloaded, totalBytes)
                }

                sink.flush()
                sink.close()
                source.close()
                response.close()

                _progress.value = DownloadProgress.Completed(destination)
                Result.success(Unit)
            } catch (e: Exception) {
                _progress.value = DownloadProgress.Failed(e.message ?: "Unknown error")
                Result.failure(e)
            }
        }

    override suspend fun cancelDownload() {
        currentJob?.cancel()
        _progress.value = DownloadProgress.Cancelled
    }

    override suspend fun deleteModel(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val file = File(context.filesDir, "models/gemma-3-1b-it-int4.task")
            if (file.exists()) {
                file.delete()
            }
            _progress.value = DownloadProgress.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
