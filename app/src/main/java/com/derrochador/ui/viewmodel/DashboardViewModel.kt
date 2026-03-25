package com.derrochador.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.derrochador.agents.BrainAgent
import com.derrochador.agents.SummaryAgent
import com.derrochador.data.model.Transaction
import com.derrochador.data.model.TransactionSource
import com.derrochador.data.repository.TransactionRepository
import com.derrochador.domain.usecase.DailySummary
import com.derrochador.domain.usecase.GetDailySummaryUseCase
import com.derrochador.domain.usecase.SaveTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val transactions: List<Transaction> = emptyList(),
    val dailySummary: DailySummary? = null,
    val summaryText: String = "",
    val isModelReady: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val getDailySummaryUseCase: GetDailySummaryUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val brainAgent: BrainAgent,
    private val summaryAgent: SummaryAgent
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        loadDailySummary()
        checkModelStatus()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAll().collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }

    private fun loadDailySummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val summary = getDailySummaryUseCase()
            val summaryText = summaryAgent.getDailySummary()
            _uiState.value = _uiState.value.copy(
                dailySummary = summary,
                summaryText = summaryText,
                isLoading = false
            )
        }
    }

    private fun checkModelStatus() {
        _uiState.value = _uiState.value.copy(isModelReady = brainAgent.isModelReady())
    }

    fun refreshSummary() {
        loadDailySummary()
    }

    fun saveManualTransaction(
        amount: Double,
        description: String,
        category: String
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                description = description,
                category = category,
                source = TransactionSource.MANUAL,
                rawText = description,
                isProcessed = true
            )
            saveTransactionUseCase(transaction)
            refreshSummary()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
            refreshSummary()
        }
    }
}
