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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private var downloadJob: Job? = null

    override suspend fun doWork(): Result {
        Log.d("ModelDownload", "=== Worker doWork() STARTED ===")
        
        val modelUrl = inputData.getString(KEY_MODEL_URL)
        val destinationPath = inputData.getString(KEY_DESTINATION_PATH)
        
        if (modelUrl == null) {
            Log.e("ModelDownload", "ERROR: modelUrl is null")
            return Result.failure()
        }
        if (destinationPath == null) {
            Log.e("ModelDownload", "ERROR: destinationPath is null")
            return Result.failure()
        }

        Log.d("ModelDownload", "Starting download from: $modelUrl")
        Log.d("ModelDownload", "Destination: $destinationPath")

        try {
            setForeground(createForegroundInfo(0, null))
            Log.d("ModelDownload", "Foreground service set up successfully")
        } catch (e: Exception) {
            val errorMsg = "Failed to start foreground service: ${e.message}"
            Log.e("ModelDownload", "ERROR: $errorMsg", e)
            val outputData = Data.Builder().putString("error_message", errorMsg).build()
            return Result.failure(outputData)
        }

        return withContext(ioDispatcher) {
            try {
                Log.d("ModelDownload", "Starting download process in IO dispatcher")
                
                val file = File(destinationPath)
                file.parentFile?.mkdirs()

                val existingSize = if (file.exists()) file.length() else 0L
                Log.d("ModelDownload", "Existing file size: $existingSize bytes")

                val request = Request.Builder().apply {
                    url(modelUrl)
                    if (existingSize > 0) {
                        header("Range", "bytes=$existingSize-")
                    }
                }.build()

                Log.d("ModelDownload", "Making HTTP request...")
                setProgressAsync(
                    Data.Builder()
                        .putLong("bytes_downloaded", 0)
                        .putLong("total_bytes", 0)
                        .putInt("progress", 0)
                        .build()
                )
                
                val response = try {
                    client.newCall(request).execute()
                } catch (e: Exception) {
                    val errorMsg = "Network error: ${e.javaClass.simpleName}: ${e.message}"
                    Log.e("ModelDownload", "ERROR: $errorMsg", e)
                    return@withContext Result.retry()
                }

                Log.d("ModelDownload", "HTTP response received. Code: ${response.code}")

                if (!response.isSuccessful && response.code != 206) {
                    val errorMsg = "HTTP error ${response.code}: ${response.message}"
                    Log.e("ModelDownload", "Download failed - $errorMsg")
                    val outputData = Data.Builder().putString("error_message", errorMsg).build()
                    response.close()
                    return@withContext if (response.code in 500..599) Result.retry() else Result.failure(outputData)
                }

                val body = response.body ?: run {
                    val errorMsg = "Response body is null"
                    Log.e("ModelDownload", "ERROR: $errorMsg")
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

                Log.d("ModelDownload", "Starting download loop...")
                
                setProgressAsync(
                    Data.Builder()
                        .putLong("bytes_downloaded", bytesDownloaded)
                        .putLong("total_bytes", totalBytes ?: 0L)
                        .putInt("progress", 0)
                        .build()
                )

                while (!isStopped) {
                    val read = try {
                        source.read(buffer, 0, buffer.size)
                    } catch (e: Exception) {
                        Log.e("ModelDownload", "ERROR: Failed to read from source", e)
                        break
                    }
                    
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
                        
                        if (bytesDownloaded % (1024 * 1024) < 8192) {
                            Log.d("ModelDownload", "Progress: ${bytesDownloaded / (1024 * 1024)}MB downloaded")
                        }
                    }
                }

                sink.flush()
                sink.close()
                source.close()
                response.close()

                setForeground(createForegroundInfo(bytesDownloaded, totalBytes))
                Log.d("ModelDownload", "Download completed successfully. Total: $bytesDownloaded bytes")
                Result.success()
            } catch (e: Exception) {
                Log.e("ModelDownload", "ERROR: Download failed with exception", e)
                if (isStopped) {
                    Log.d("ModelDownload", "Worker was stopped by user")
                    val errorMsg = "Download cancelled by user"
                    val outputData = Data.Builder().putString("error_message", errorMsg).build()
                    Result.failure(outputData)
                } else {
                    Log.d("ModelDownload", "Will retry due to transient error: ${e.message}")
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
