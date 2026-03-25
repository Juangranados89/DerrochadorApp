package com.derrochador.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String = "Sin categoría",
    val source: String, // "notification" or "sms"
    val packageName: String = "",
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDuplicate: Boolean = false
)
