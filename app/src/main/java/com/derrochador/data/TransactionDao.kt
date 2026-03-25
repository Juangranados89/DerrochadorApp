package com.derrochador.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE rawText = :rawText AND timestamp > :sinceTimestamp LIMIT 1")
    suspend fun findDuplicate(rawText: String, sinceTimestamp: Long): TransactionEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime AND isDuplicate = 0")
    fun getTotalSpentBetween(startTime: Long, endTime: Long): Flow<Double?>
}
