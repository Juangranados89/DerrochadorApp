package com.derrochador.data.repository

import com.derrochador.data.dao.CategoryTotal
import com.derrochador.data.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getByCategory(category: String): Flow<List<Transaction>>
    suspend fun getUnprocessed(): List<Transaction>
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun getDailyTotal(startOfDay: Long, endOfDay: Long): List<CategoryTotal>
}
