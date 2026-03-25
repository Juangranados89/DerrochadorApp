package com.derrochador.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun findDuplicate(rawText: String, withinMillis: Long = 300_000): TransactionEntity? =
        transactionDao.findDuplicate(rawText, System.currentTimeMillis() - withinMillis)

    fun getTotalSpentBetween(startTime: Long, endTime: Long): Flow<Double?> =
        transactionDao.getTotalSpentBetween(startTime, endTime)
}
