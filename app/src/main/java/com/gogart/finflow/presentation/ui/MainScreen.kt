package com.gogart.finflow.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gogart.finflow.R
import com.gogart.finflow.data.local.entity.TransactionEntity
import com.gogart.finflow.data.local.entity.TransactionWithCategory
import com.gogart.finflow.presentation.ui.util.CategoryIconHelper
import com.gogart.finflow.presentation.ui.util.CurrencyFormatter
import com.gogart.finflow.presentation.viewmodel.TransactionViewModel
import com.gogart.finflow.ui.theme.FinFlowExtendedColors
import com.gogart.finflow.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionFilter(val stringResId: Int) {
    ALL(R.string.all),
    INCOME(R.string.incomes),
    EXPENSE(R.string.expenses)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var filter by remember { mutableStateOf(TransactionFilter.ALL) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var deletingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredTransactions = when (filter) {
        TransactionFilter.ALL -> transactions
        TransactionFilter.INCOME -> transactions.filter { it.transaction.isIncome }
        TransactionFilter.EXPENSE -> transactions.filter { !it.transaction.isIncome }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
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
                onClick = {
                    editingTransaction = null
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_transaction),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.m)
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))

            // Balance Summary Card
            BalanceCard(
                totalBalance = totalBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                currencySymbol = currencySymbol
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier.fillMaxWidth()
            ) {
                TransactionFilter.entries.forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(stringResource(item.stringResId), style = MaterialTheme.typography.labelLarge) },
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Transactions Header
            Text(
                text = stringResource(R.string.transaction_history, filteredTransactions.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = Spacing.s)
            )

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Text(
                            text = stringResource(R.string.empty_transactions),
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredTransactions,
                        key = { it.transaction.id }
                    ) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        deletingTransaction = item.transaction
                                        false
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        editingTransaction = item
                                        showBottomSheet = true
                                        false
                                    }
                                    SwipeToDismissBoxValue.Settled -> false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(color)
                                        .padding(horizontal = Spacing.m),
                                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onError)
                                    } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_transaction), tint = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        ) {
                            TransactionItemCard(
                                item = item,
                                currencySymbol = currencySymbol,
                                onClick = {
                                    editingTransaction = item
                                    showBottomSheet = true
                                },
                                onDelete = { deletingTransaction = item.transaction }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            AddTransactionBottomSheet(
                sheetState = sheetState,
                categories = categories,
                accounts = accounts,
                currencySymbol = currencySymbol,
                existingTransaction = editingTransaction,
                onDismiss = {
                    showBottomSheet = false
                    editingTransaction = null
                },
                onSaveNew = { title, amount, isIncome, categoryId, accountId ->
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        isIncome = isIncome,
                        categoryId = categoryId,
                        accountId = accountId
                    )
                },
                onSaveExisting = { updated ->
                    viewModel.updateTransaction(updated)
                }
            )
        }

        deletingTransaction?.let { tx ->
            AlertDialog(
                onDismissRequest = { deletingTransaction = null },
                title = { Text(stringResource(R.string.delete_confirm_title), style = MaterialTheme.typography.titleMedium) },
                text = { Text(stringResource(R.string.delete_confirm_msg, tx.title), style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            deletingTransaction = null
                        }
                    ) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingTransaction = null }) {
                        Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    }
}

@Composable
fun BalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencySymbol: String
) {
    val isDark = isSystemInDarkTheme()

    val incomeColor = if (isDark) FinFlowExtendedColors.IncomeDark else FinFlowExtendedColors.IncomeLight
    val expenseColor = if (isDark) FinFlowExtendedColors.ExpenseDark else FinFlowExtendedColors.ExpenseLight

    val animatedBalance by animateFloatAsState(targetValue = totalBalance.toFloat(), label = "balanceAnimation")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.m)
        ) {
            Text(
                text = stringResource(R.string.total_balance),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = CurrencyFormatter.formatAmount(animatedBalance.toDouble(), currencySymbol),
                style = MaterialTheme.typography.headlineLarge,
                color = if (animatedBalance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(Spacing.m))

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
                            .background(incomeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = stringResource(R.string.incomes),
                            tint = incomeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Column {
                        Text(stringResource(R.string.incomes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = CurrencyFormatter.formatAmount(totalIncome, currencySymbol),
                            style = MaterialTheme.typography.titleSmall,
                            color = incomeColor
                        )
                    }
                }

                // Expense Summary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(expenseColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = stringResource(R.string.expenses),
                            tint = expenseColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Column {
                        Text(stringResource(R.string.expenses), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = CurrencyFormatter.formatAmount(totalExpense, currencySymbol),
                            style = MaterialTheme.typography.titleSmall,
                            color = expenseColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    item: TransactionWithCategory,
    currencySymbol: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val transaction = item.transaction
    val category = item.category
    val isDark = isSystemInDarkTheme()

    val categoryColor = CategoryIconHelper.parseColorHex(category.colorHex)
    val categoryIcon = CategoryIconHelper.getIconByName(category.iconName)

    val incomeColor = if (isDark) FinFlowExtendedColors.IncomeDark else FinFlowExtendedColors.IncomeLight
    val expenseColor = if (isDark) FinFlowExtendedColors.ExpenseDark else FinFlowExtendedColors.ExpenseLight
    val amountColor = if (transaction.isIncome) incomeColor else expenseColor

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.s + Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = category.name,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.s + Spacing.xs))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${category.name} • $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = "${if (transaction.isIncome) "+" else "-"}${CurrencyFormatter.formatAmount(transaction.amount, currencySymbol)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
