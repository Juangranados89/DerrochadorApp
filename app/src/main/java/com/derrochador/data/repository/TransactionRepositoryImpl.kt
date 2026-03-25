package com.derrochador.data.repository

import com.derrochador.data.dao.CategoryTotal
import com.derrochador.data.dao.TransactionDao
import com.derrochador.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> = dao.getAll()

    override fun getByCategory(category: String): Flow<List<Transaction>> =
        dao.getByCategory(category)

    override suspend fun getUnprocessed(): List<Transaction> = dao.getUnprocessed()

    override suspend fun insert(transaction: Transaction): Long = dao.insert(transaction)

    override suspend fun update(transaction: Transaction) = dao.update(transaction)

    override suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    override suspend fun getDailyTotal(startOfDay: Long, endOfDay: Long): List<CategoryTotal> =
        dao.getDailyTotal(startOfDay, endOfDay)
}
