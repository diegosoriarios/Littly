package com.diego.littly.core.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.diego.littly.core.common.di.ApplicationScope
import com.diego.littly.core.common.di.IoDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) : CoroutineWorker(appContext, workerParams) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .build()
    private var downloadJob: Job? = null

    override suspend fun doWork(): Result {
        val modelUrl = inputData.getString(KEY_MODEL_URL) ?: return Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION_PATH) ?: return Result.failure()

        Log.d("ModelDownload", "Starting download from: $modelUrl")
        Log.d("ModelDownload", "Destination: $destinationPath")

        setForeground(createForegroundInfo(0, null))

        return withContext(ioDispatcher) {
            try {
                val file = File(destinationPath)
                file.parentFile?.mkdirs()

                val existingSize = if (file.exists()) file.length() else 0L

                val request = Request.Builder().apply {
                    url(modelUrl)
                    if (existingSize > 0) {
                        header("Range", "bytes=$existingSize-")
                    }
                }.build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful && response.code != 206) {
                    Log.e("ModelDownload", "Download failed with response code: ${response.code}")
                    response.close()
                    return@withContext Result.retry()
                }

                val body = response.body ?: run {
                    Log.e("ModelDownload", "Response body is null")
                    response.close()
                    return@withContext Result.retry()
                }

                val contentLength = body.contentLength()
                val totalBytes = if (response.code == 206) {
                    existingSize + contentLength
                } else {
                    contentLength
                }

                Log.d("ModelDownload", "Response code: ${response.code}, Total bytes: $totalBytes")

                val append = response.code == 206
                val sink = file.sink(append).buffer()
                val source = body.source()

                var bytesDownloaded = existingSize
                val buffer = ByteArray(8192)
                var lastNotificationUpdate = 0L

                // Report initial progress immediately
                setProgressAsync(
                    Data.Builder()
                        .putLong("bytes_downloaded", bytesDownloaded)
                        .putLong("total_bytes", totalBytes ?: 0L)
                        .putInt("progress", 0)
                        .build()
                )

                while (!isStopped) {
                    val read = source.read(buffer, 0, buffer.size)
                    if (read == -1) break

                    sink.write(buffer, 0, read)
                    bytesDownloaded += read

                    val now = System.currentTimeMillis()
                    if (now - lastNotificationUpdate > 500) {
                        val progress = if (totalBytes != null && totalBytes > 0) {
                            (bytesDownloaded * 100 / totalBytes).toInt()
                        } else {
                            0
                        }
                        
                        setForeground(createForegroundInfo(bytesDownloaded, totalBytes))
                        setProgressAsync(
                            Data.Builder()
                                .putLong("bytes_downloaded", bytesDownloaded)
                                .putLong("total_bytes", totalBytes ?: 0L)
                                .putInt("progress", progress)
                                .build()
                        )
                        lastNotificationUpdate = now
                    }
                }

                sink.flush()
                sink.close()
                source.close()
                response.close()

                setForeground(createForegroundInfo(bytesDownloaded, totalBytes))
                Log.d("ModelDownload", "Download completed successfully")
                Result.success()
            } catch (e: Exception) {
                Log.e("ModelDownload", "Download failed", e)
                if (isStopped) {
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
        }
    }

    private fun createForegroundInfo(bytesDownloaded: Long, totalBytes: Long?): ForegroundInfo {
        val channelId = "model_download"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Downloads for the offline idea engine"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val progress = if (totalBytes != null && totalBytes > 0) {
            (bytesDownloaded * 100 / totalBytes).toInt()
        } else {
            0
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading idea engine")
            .setProgress(100, progress, totalBytes == null)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()

        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }

        return foregroundInfo
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0, null)
    }

    companion object {
        const val KEY_MODEL_URL = "model_url"
        const val KEY_DESTINATION_PATH = "destination_path"
        const val WORK_NAME = "model_download"
        private const val NOTIFICATION_ID = 1001
    }
}
