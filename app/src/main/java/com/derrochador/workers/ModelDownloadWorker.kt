package com.derrochador.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val TAG = "ModelDownloadWorker"
const val KEY_MODEL_URL = "model_url"
const val KEY_MODEL_PATH = "model_path"

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val modelUrl = inputData.getString(KEY_MODEL_URL) ?: return Result.failure(
            workDataOf("error" to "No model URL provided")
        )
        val modelPath = inputData.getString(KEY_MODEL_PATH)
            ?: "${applicationContext.filesDir.absolutePath}/gemma-2b-it-cpu-int4.bin"

        return try {
            val outputFile = File(modelPath)
            outputFile.parentFile?.mkdirs()

            setProgress(workDataOf("progress" to 0))

            URL(modelUrl).openStream().use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                        val progressMb = (totalBytes / (1024 * 1024)).toInt()
                        setProgress(workDataOf("progress_mb" to progressMb))
                    }
                }
            }

            Log.i(TAG, "Model downloaded successfully to $modelPath")
            Result.success(workDataOf("model_path" to modelPath))
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading model: ${e.message}", e)
            Result.failure(workDataOf("error" to e.message))
        }
    }

    companion object {
        fun buildRequest(modelUrl: String, modelPath: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                .setInputData(
                    workDataOf(
                        KEY_MODEL_URL to modelUrl,
                        KEY_MODEL_PATH to modelPath
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresStorageNotLow(true)
                        .build()
                )
                .addTag("model_download")
                .build()
        }
    }
}
