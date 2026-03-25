package com.derrochador.agents

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import com.derrochador.data.model.Transaction
import com.derrochador.data.model.TransactionSource
import com.derrochador.domain.usecase.ClassifyExpenseUseCase
import com.derrochador.domain.usecase.SaveTransactionUseCase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "VisionAgent"
private val AMOUNT_REGEX = Regex("""[$\$]?\s*\d{1,3}(?:[,.]?\d{3})*(?:[.,]\d{2})?""")

@AndroidEntryPoint
class VisionAgent : Service() {

    @Inject
    lateinit var saveTransactionUseCase: SaveTransactionUseCase

    @Inject
    lateinit var classifyExpenseUseCase: ClassifyExpenseUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var fileObserver: FileObserver? = null
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startObserving()
        return START_STICKY
    }

    private fun startObserving() {
        val whatsappDir = File(
            android.os.Environment.getExternalStorageDirectory(),
            "Pictures/WhatsApp"
        )
        if (!whatsappDir.exists()) {
            Log.w(TAG, "WhatsApp pictures directory not found")
            return
        }

        fileObserver = object : FileObserver(whatsappDir, CREATE or CLOSE_WRITE) {
            override fun onEvent(event: Int, path: String?) {
                if (path == null) return
                if (!path.endsWith(".jpg", ignoreCase = true) &&
                    !path.endsWith(".jpeg", ignoreCase = true) &&
                    !path.endsWith(".png", ignoreCase = true)
                ) return

                val file = File(whatsappDir, path)
                processImage(file)
            }
        }.also { it.startWatching() }
    }

    private fun processImage(file: File) {
        serviceScope.launch {
            try {
                val image = InputImage.fromFilePath(this@VisionAgent, Uri.fromFile(file))
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        if (extractedText.isBlank()) return@addOnSuccessListener

                        val amount = extractAmount(extractedText)
                        if (amount <= 0.0) return@addOnSuccessListener

                        serviceScope.launch {
                            val category = classifyExpenseUseCase(extractedText)
                            val transaction = Transaction(
                                amount = amount,
                                description = "Captura de pantalla",
                                category = category.name,
                                source = TransactionSource.SCREENSHOT,
                                rawText = extractedText,
                                isProcessed = true
                            )
                            saveTransactionUseCase(transaction)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error processing image: ${e.message}", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading image file: ${e.message}", e)
            }
        }
    }

    private fun extractAmount(text: String): Double {
        val match = AMOUNT_REGEX.find(text) ?: return 0.0
        return match.value
            .replace("$", "")
            .replace(",", "")
            .replace(" ", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }

    override fun onDestroy() {
        fileObserver?.stopWatching()
        textRecognizer.close()
        super.onDestroy()
    }
}
