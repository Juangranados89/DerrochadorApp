package com.derrochador.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.derrochador.data.model.Transaction
import com.derrochador.domain.model.ExpenseCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filtered = if (selectedCategory == null) {
        transactions
    } else {
        transactions.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas las transacciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category filter chips
            ScrollableTabRow(
                selectedTabIndex = if (selectedCategory == null) 0
                else ExpenseCategory.values().indexOfFirst { it.name == selectedCategory } + 1,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    text = { Text("Todas") }
                )
                ExpenseCategory.values().forEach { category ->
                    Tab(
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name },
                        text = { Text(category.displayName) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin transacciones en esta categoría",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { transaction ->
                        TransactionCard(transaction = transaction, onDelete = onDelete)
                    }
                }
            }
        }
    }
}
