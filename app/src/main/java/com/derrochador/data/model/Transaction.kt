package com.derrochador.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionSource {
    NOTIFICATION, SCREENSHOT, MANUAL
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val source: TransactionSource,
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false,
    val isDuplicate: Boolean = false
)
