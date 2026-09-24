package com.gogart.finflow.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.presentation.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionFilter(val label: String) {
    ALL("Всі"),
    INCOME("Доходи"),
    EXPENSE("Витрати")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()

    var filter by remember { mutableStateOf(TransactionFilter.ALL) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredTransactions = when (filter) {
        TransactionFilter.ALL -> transactions
        TransactionFilter.INCOME -> transactions.filter { it.isIncome }
        TransactionFilter.EXPENSE -> transactions.filter { !it.isIncome }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FinFlow",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Додати транзакцію",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Balance Summary Card
            BalanceCard(
                totalBalance = totalBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TransactionFilter.entries.forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(item.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions Header
            Text(
                text = "Історія операцій (${filteredTransactions.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Операцій поки немає.\nНатисніть +, щоб додати першу транзакцію.",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredTransactions,
                        key = { it.id }
                    ) { item ->
                        TransactionItemCard(
                            transaction = item,
                            onDelete = { viewModel.deleteTransaction(item) }
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            AddTransactionBottomSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onSave = { title, amount, isIncome, category ->
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        isIncome = isIncome,
                        category = category
                    )
                }
            )
        }
    }
}

@Composable
fun BalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Загальний баланс",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = String.format(Locale.getDefault(), "%.2f ₴", totalBalance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (totalBalance >= 0) MaterialTheme.colorScheme.onSurface else Color(0xFFC62828)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income Summary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Доходи",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Доходи", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = String.format(Locale.getDefault(), "+%.2f ₴", totalIncome),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Expense Summary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Витрати",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Витрати", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = String.format(Locale.getDefault(), "-%.2f ₴", totalExpense),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (transaction.isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (transaction.isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${transaction.category} • $formattedDate",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%s%.2f ₴",
                    if (transaction.isIncome) "+" else "-",
                    transaction.amount
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Видалити",
                    tint = Color.Gray
                )
            }
        }
    }
}
