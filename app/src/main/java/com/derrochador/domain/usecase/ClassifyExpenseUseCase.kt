package com.derrochador.domain.usecase

import com.derrochador.domain.model.ExpenseCategory
import javax.inject.Inject

class ClassifyExpenseUseCase @Inject constructor() {

    operator fun invoke(rawText: String): ExpenseCategory {
        val normalizedText = rawText.lowercase().trim()
        for (category in ExpenseCategory.values()) {
            if (category == ExpenseCategory.OTHER) continue
            for (keyword in category.keywords) {
                if (normalizedText.contains(keyword)) {
                    return category
                }
            }
        }
        return ExpenseCategory.OTHER
    }
}
