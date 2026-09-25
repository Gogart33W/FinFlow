package com.gogart.finflow.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gogart.finflow.R
import com.gogart.finflow.data.local.entity.AccountType
import com.gogart.finflow.data.local.entity.AccountWithBalance
import com.gogart.finflow.presentation.ui.util.CategoryIconHelper
import com.gogart.finflow.presentation.ui.util.CurrencyFormatter
import com.gogart.finflow.presentation.viewmodel.AccountViewModel
import com.gogart.finflow.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(accountViewModel: AccountViewModel) {
    val accountsWithBalance by accountViewModel.accountsWithBalance.collectAsState()

    var showAddBottomSheet by remember { mutableStateOf(false) }
    var showCannotDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.accounts_title),
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
                onClick = { showAddBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_account),
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xs),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = accountsWithBalance,
                    key = { it.account.id }
                ) { item ->
                    AccountCardItem(
                        accountWithBalance = item,
                        onDelete = {
                            accountViewModel.deleteAccount(
                                account = item.account,
                                onCannotDelete = { showCannotDeleteDialog = true }
                            )
                        }
                    )
                }
            }
        }

        if (showAddBottomSheet) {
            AddAccountBottomSheet(
                sheetState = sheetState,
                onDismiss = { showAddBottomSheet = false },
                onSave = { name, type, initialBalance, colorHex ->
                    accountViewModel.addAccount(
                        name = name,
                        type = type,
                        initialBalance = initialBalance,
                        colorHex = colorHex
                    )
                }
            )
        }

        if (showCannotDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showCannotDeleteDialog = false },
                title = { Text(stringResource(R.string.cannot_delete_account_title), style = MaterialTheme.typography.titleMedium) },
                text = { Text(stringResource(R.string.cannot_delete_account_msg), style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = { showCannotDeleteDialog = false }) {
                        Text(stringResource(R.string.ok), style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    }
}

@Composable
fun AccountCardItem(
    accountWithBalance: AccountWithBalance,
    onDelete: () -> Unit
) {
    val account = accountWithBalance.account
    val currencySymbol = stringResource(R.string.currency_symbol)
    val color = CategoryIconHelper.parseColorHex(account.colorHex)

    val icon = when (account.type) {
        AccountType.CASH -> Icons.Default.MonetizationOn
        AccountType.CARD -> Icons.Default.CreditCard
        AccountType.BANK -> Icons.Default.AccountBalanceWallet
        AccountType.SAVINGS -> Icons.Default.Savings
        AccountType.OTHER -> Icons.Default.AccountBalanceWallet
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = account.name,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.m))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (account.isDefault) {
                        Spacer(modifier = Modifier.width(Spacing.s))
                        Text(
                            text = "(Основний)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = account.type.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = CurrencyFormatter.formatAmount(accountWithBalance.currentBalance, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (accountWithBalance.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            }

            if (!account.isDefault) {
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAccountBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (name: String, type: AccountType, initialBalance: Double, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("0.0") }
    var selectedType by remember { mutableStateOf(AccountType.CARD) }
    var selectedColorHex by remember { mutableStateOf("#2196F3") }
    var isError by remember { mutableStateOf(false) }

    val colors = listOf("#4CAF50", "#2196F3", "#9C27B0", "#FF9800", "#E91E63", "#009688", "#795548")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.m + Spacing.xs, vertical = Spacing.s + Spacing.xs)
        ) {
            Text(
                text = stringResource(R.string.add_account),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Spacing.m)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isError = false
                },
                label = { Text(stringResource(R.string.account_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError && name.isBlank()
            )

            Spacer(modifier = Modifier.height(Spacing.s + Spacing.xs))

            OutlinedTextField(
                value = initialBalanceText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d{0,2}$"))) {
                        initialBalanceText = it
                        isError = false
                    }
                },
                label = { Text(stringResource(R.string.initial_balance_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = stringResource(R.string.account_type_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = Spacing.s)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier.fillMaxWidth()
            ) {
                AccountType.entries.forEach { type ->
                    FilterChip(
                        selected = type == selectedType,
                        onClick = { selectedType = type },
                        label = { Text(type.title) },
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = "Колір рахунку:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = Spacing.s)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                colors.forEach { colorHex ->
                    val color = CategoryIconHelper.parseColorHex(colorHex)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { selectedColorHex = colorHex },
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xs)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        val initBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            onSave(name.trim(), selectedType, initBal, selectedColorHex)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.l))
        }
    }
}
