package com.derrochador.domain

/**
 * Parses raw notification text from banking apps to extract transaction data.
 * Supports common patterns from Latin American banks (pesos, USD).
 */
object NotificationParser {

    data class ParsedTransaction(
        val amount: Double,
        val description: String
    )

    private val DESC_PATTERNS = listOf(
        Regex("""(?i)(?:en|comercio|establecimiento)\s+(.+?)(?:\.|,|\s+por|\s+el|\s+a\s+las|$)"""),
        Regex("""(?i)(?:compra|pago|transferencia)\s+(?:en\s+)?(.+?)(?:\.|,|\s+por|$)""")
    )

    private val AMOUNT_PATTERNS = listOf(
        // Patterns like "$1,234.56" or "$1.234,56" or "COP 1,234"
        Regex("""(?i)(?:cop|usd|mxn|ars)?\s*\$?\s*([\d.,]+)"""),
        // Patterns like "por valor de $50,000"
        Regex("""(?i)(?:por\s+(?:valor\s+de|un\s+monto\s+de))\s*\$?\s*([\d.,]+)"""),
        // Patterns like "monto: 50000"
        Regex("""(?i)monto\s*[:=]\s*\$?\s*([\d.,]+)""")
    )

    /**
     * Attempts to parse a bank notification text to extract the transaction amount
     * and a short description. Returns null if no amount pattern is found.
     */
    fun parse(text: String): ParsedTransaction? {
        val amount = extractAmount(text) ?: return null
        val description = extractDescription(text)
        return ParsedTransaction(amount = amount, description = description)
    }

    private fun extractAmount(text: String): Double? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                val rawAmount = match.groupValues[1]
                return normalizeAmount(rawAmount)
            }
        }
        return null
    }

    private fun normalizeAmount(raw: String): Double? {
        // Handle both "1,234.56" and "1.234,56" formats
        val cleaned = if (raw.contains(',') && raw.contains('.')) {
            if (raw.lastIndexOf(',') > raw.lastIndexOf('.')) {
                // European/Latin format: 1.234,56
                raw.replace(".", "").replace(",", ".")
            } else {
                // US format: 1,234.56
                raw.replace(",", "")
            }
        } else if (raw.contains(',')) {
            // Could be "1,234" (thousands) or "1,56" (decimal)
            val afterComma = raw.substringAfter(',')
            if (afterComma.length <= 2) {
                raw.replace(",", ".")
            } else {
                raw.replace(",", "")
            }
        } else {
            raw
        }
        return cleaned.toDoubleOrNull()
    }

    private fun extractDescription(text: String): String {
        for (pattern in DESC_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim().take(100)
            }
        }
        // Fallback: first 80 chars of original text
        return text.take(80).trim()
    }
}
