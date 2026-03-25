package com.derrochador.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.derrochador.data.model.Transaction
import com.derrochador.domain.model.ExpenseCategory
import com.derrochador.ui.theme.*
import com.derrochador.ui.viewmodel.DashboardUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))

fun categoryColor(categoryName: String): Color = when (categoryName) {
    ExpenseCategory.FOOD.name -> CategoryFood
    ExpenseCategory.TRANSPORT.name -> CategoryTransport
    ExpenseCategory.TRANSFER.name -> CategoryTransfer
    ExpenseCategory.SHOPPING.name -> CategoryShopping
    ExpenseCategory.ENTERTAINMENT.name -> CategoryEntertainment
    ExpenseCategory.HEALTH.name -> CategoryHealth
    ExpenseCategory.UTILITIES.name -> CategoryUtilities
    else -> CategoryOther
}

fun categoryDisplayName(categoryName: String): String =
    ExpenseCategory.values().firstOrNull { it.name == categoryName }?.displayName ?: categoryName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAddTransaction: () -> Unit,
    onRefresh: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Derrochador", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Agregar gasto")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AI Model status chip
            item {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (uiState.isModelReady) "IA local activa" else "IA: modo básico",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }

            // Daily summary card
            item {
                DailySummaryCard(uiState = uiState)
            }

            // Category breakdown
            if (uiState.dailySummary != null && uiState.dailySummary.categoryTotals.isNotEmpty()) {
                item {
                    CategoryBreakdownCard(uiState = uiState)
                }
            }

            // Recent transactions header
            item {
                Text(
                    "Transacciones recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sin transacciones aún.\nAgrega una con el botón +",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.transactions.take(10)) { transaction ->
                    TransactionCard(transaction = transaction, onDelete = onDeleteTransaction)
                }
            }
        }
    }
}

@Composable
private fun DailySummaryCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Resumen de hoy",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                currencyFormat.format(uiState.dailySummary?.totalAmount ?: 0.0),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (uiState.summaryText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    uiState.summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(uiState: DashboardUiState) {
    val totals = uiState.dailySummary?.categoryTotals ?: return
    val maxTotal = totals.maxOfOrNull { it.total } ?: 1.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Por categoría",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            totals.forEach { ct ->
                val fraction = (ct.total / maxTotal).toFloat().coerceIn(0f, 1f)
                val color = categoryColor(ct.category)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        categoryDisplayName(ct.category),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(100.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .background(color, shape = MaterialTheme.shapes.small)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        currencyFormat.format(ct.total),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: (Transaction) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar transacción") },
            text = { Text("¿Deseas eliminar esta transacción?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(transaction)
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDeleteDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        categoryColor(transaction.category),
                        shape = MaterialTheme.shapes.small
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${categoryDisplayName(transaction.category)} • ${dateFormat.format(Date(transaction.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                currencyFormat.format(transaction.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ExpenseRed
            )
        }
    }
}
