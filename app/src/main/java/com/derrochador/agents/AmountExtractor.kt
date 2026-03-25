package com.derrochador.agents

/**
 * Extracts a monetary amount from a string.
 * Handles both US format (1,234.56) and European/MX format (1.234,56).
 * Returns 0.0 if no valid amount is found.
 */
internal fun extractAmount(text: String): Double {
    // Match an optional currency symbol followed by digits with optional separators
    val regex = Regex("""[$＄]?\s*(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{1,2})?)""")
    val match = regex.find(text) ?: return 0.0
    val raw = match.groupValues[1]

    return when {
        // European format: last separator is comma with exactly 2 digits (e.g., "1.234,56")
        raw.matches(Regex("""\d{1,3}(?:\.\d{3})*,\d{1,2}""")) ->
            raw.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        // US/MX format: last separator is period with exactly 2 digits (e.g., "1,234.56")
        raw.matches(Regex("""\d{1,3}(?:,\d{3})*\.\d{1,2}""")) ->
            raw.replace(",", "").toDoubleOrNull() ?: 0.0
        // No separator or only thousands separator (e.g., "1234" or "1,234")
        else ->
            raw.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
    }
}
