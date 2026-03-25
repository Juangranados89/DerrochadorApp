package com.derrochador.agents

import android.content.Context
import android.util.Log
import com.derrochador.domain.model.ExpenseCategory
import com.derrochador.domain.usecase.ClassifyExpenseUseCase
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BrainAgent"
private const val MODEL_FILENAME = "gemma-2b-it-cpu-int4.bin"

const val CATEGORIZATION_PROMPT_TEMPLATE = """
Eres un agente financiero. Clasifica la siguiente transacción en una de estas categorías:
FOOD, TRANSPORT, TRANSFER, SHOPPING, ENTERTAINMENT, HEALTH, UTILITIES, OTHER.

Texto de la transacción: "{TEXT}"

Responde ÚNICAMENTE con el nombre de la categoría en mayúsculas, sin explicación adicional.
Categoría:""".trimIndent()

@Singleton
class BrainAgent @Inject constructor(
    private val context: Context,
    private val classifyExpenseUseCase: ClassifyExpenseUseCase
) {

    private var llmInference: LlmInference? = null
    private var isModelAvailable = false

    init {
        tryInitModel()
    }

    private fun tryInitModel() {
        try {
            val modelPath = "${context.filesDir.absolutePath}/$MODEL_FILENAME"
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .build()
            llmInference = LlmInference.createFromOptions(context, options)
            isModelAvailable = true
            Log.i(TAG, "LLM model loaded successfully")
        } catch (e: Exception) {
            Log.w(TAG, "LLM model not available, falling back to keyword matching: ${e.message}")
            isModelAvailable = false
        }
    }

    fun isModelReady(): Boolean = isModelAvailable

    suspend fun classifyExpense(rawText: String): ExpenseCategory = withContext(Dispatchers.Default) {
        if (!isModelAvailable || llmInference == null) {
            return@withContext classifyExpenseUseCase(rawText)
        }
        try {
            val prompt = CATEGORIZATION_PROMPT_TEMPLATE.replace("{TEXT}", rawText)
            val result = llmInference!!.generateResponse(prompt).trim().uppercase()
            ExpenseCategory.values().firstOrNull { it.name == result }
                ?: classifyExpenseUseCase(rawText)
        } catch (e: Exception) {
            Log.e(TAG, "LLM inference error, falling back to keywords: ${e.message}")
            classifyExpenseUseCase(rawText)
        }
    }

    suspend fun generateSummaryText(prompt: String): String = withContext(Dispatchers.Default) {
        if (!isModelAvailable || llmInference == null) {
            return@withContext ""
        }
        try {
            llmInference!!.generateResponse(prompt).trim()
        } catch (e: Exception) {
            Log.e(TAG, "LLM generation error: ${e.message}")
            ""
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
        isModelAvailable = false
    }
}
