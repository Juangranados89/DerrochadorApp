package com.derrochador.agents

import com.derrochador.domain.model.ExpenseCategory
import com.derrochador.domain.usecase.DailySummary
import com.derrochador.domain.usecase.GetDailySummaryUseCase
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val SUMMARY_PROMPT_TEMPLATE = """
Eres un asistente financiero personal en español. Basándote en estos gastos del día:

{EXPENSES}

Total gastado: {TOTAL}

Genera un resumen breve y amigable en español (máximo 3 oraciones) con observaciones sobre el gasto del día.
""".trimIndent()

@Singleton
class SummaryAgent @Inject constructor(
    private val brainAgent: BrainAgent,
    private val getDailySummaryUseCase: GetDailySummaryUseCase
) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    suspend fun getDailySummary(): String {
        val summary = getDailySummaryUseCase()
        return if (brainAgent.isModelReady()) {
            generateLlmSummary(summary)
        } else {
            generateFallbackSummary(summary)
        }
    }

    private suspend fun generateLlmSummary(summary: DailySummary): String {
        val expensesText = summary.categoryTotals.joinToString("\n") { ct ->
            val displayName = ExpenseCategory.values()
                .firstOrNull { it.name == ct.category }?.displayName ?: ct.category
            "- $displayName: ${currencyFormat.format(ct.total)}"
        }
        val totalText = currencyFormat.format(summary.totalAmount)
        val prompt = SUMMARY_PROMPT_TEMPLATE
            .replace("{EXPENSES}", expensesText.ifBlank { "Sin gastos registrados" })
            .replace("{TOTAL}", totalText)

        val llmResult = brainAgent.generateSummaryText(prompt)
        return llmResult.ifBlank { generateFallbackSummary(summary) }
    }

    private fun generateFallbackSummary(summary: DailySummary): String {
        if (summary.categoryTotals.isEmpty()) {
            return "No hay gastos registrados hoy."
        }
        val total = currencyFormat.format(summary.totalAmount)
        val topCategory = summary.categoryTotals.maxByOrNull { it.total }
        val topCategoryText = topCategory?.let {
            val displayName = ExpenseCategory.values()
                .firstOrNull { v -> v.name == it.category }?.displayName ?: it.category
            "Tu mayor gasto fue en $displayName (${currencyFormat.format(it.total)})."
        } ?: ""
        return "Hoy gastaste un total de $total. $topCategoryText"
    }
}
