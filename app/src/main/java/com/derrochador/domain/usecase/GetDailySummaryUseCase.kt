package com.derrochador.domain.usecase

import com.derrochador.data.dao.CategoryTotal
import com.derrochador.data.repository.TransactionRepository
import java.util.Calendar
import javax.inject.Inject

data class DailySummary(
    val date: Long,
    val totalAmount: Double,
    val categoryTotals: List<CategoryTotal>
)

class GetDailySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {

    suspend operator fun invoke(): DailySummary {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val categoryTotals = repository.getDailyTotal(startOfDay, endOfDay)
        val totalAmount = categoryTotals.sumOf { it.total }

        return DailySummary(
            date = startOfDay,
            totalAmount = totalAmount,
            categoryTotals = categoryTotals
        )
    }
}
