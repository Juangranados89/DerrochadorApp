package com.derrochador.domain.usecase

import com.derrochador.data.model.Transaction
import com.derrochador.data.repository.TransactionRepository
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {

    suspend operator fun invoke(transaction: Transaction): Long {
        return repository.insert(transaction)
    }
}
