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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gogart.finflow.R
import com.gogart.finflow.data.local.entity.BudgetWithSpent
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.presentation.ui.util.CategoryIconHelper
import com.gogart.finflow.presentation.ui.util.CurrencyFormatter
import com.gogart.finflow.presentation.viewmodel.BudgetViewModel
import com.gogart.finflow.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(viewModel: BudgetViewModel) {
    val budgets by viewModel.budgetsWithSpent.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showAddBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.budgets_title),
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
                    contentDescription = stringResource(R.string.set_budget),
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

            if (budgets.isEmpty()) {
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
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Text(
                            text = stringResource(R.string.no_budgets_set),
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = budgets, key = { it.budget.id }) { item ->
                        BudgetCardItem(
                            budgetWithSpent = item,
                            currencySymbol = currencySymbol,
                            onDelete = { viewModel.deleteBudget(item.budget) }
                        )
                    }
                }
            }
        }

        if (showAddBottomSheet) {
            SetBudgetBottomSheet(
                sheetState = sheetState,
                categories = expenseCategories,
                currencySymbol = currencySymbol,
                onDismiss = { showAddBottomSheet = false },
                onSave = { categoryId, limit ->
                    viewModel.setBudget(categoryId, limit)
                }
            )
        }
    }
}

@Composable
fun BudgetCardItem(
    budgetWithSpent: BudgetWithSpent,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val category = budgetWithSpent.category
    val budget = budgetWithSpent.budget
    val spent = budgetWithSpent.spentAmount

    val percentage = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit) else 0.0
    val colorHex = category.colorHex
    val categoryColor = CategoryIconHelper.parseColorHex(colorHex)
    val icon = CategoryIconHelper.getIconByName(category.iconName)

    val progressColor = when {
        percentage >= 1.0 -> MaterialTheme.colorScheme.error // Red equivalent
        percentage >= 0.7 -> Color(0xFFFBC02D) // Yellow
        else -> Color(0xFF388E3C) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.m)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.s + Spacing.xs))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${stringResource(R.string.budget_limit_label)}: ${CurrencyFormatter.formatAmount(budget.monthlyLimit, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.budget_spent_label)}: ${CurrencyFormatter.formatAmount(spent, currencySymbol)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = progressColor
                )
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            LinearProgressIndicator(
                progress = { percentage.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetBudgetBottomSheet(
    sheetState: SheetState,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, limit: Double) -> Unit
) {
    var limitText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var isError by remember { mutableStateOf(false) }

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
                text = stringResource(R.string.set_budget),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = Spacing.m)
            )

            OutlinedTextField(
                value = limitText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        limitText = it
                        isError = false
                    }
                },
                label = { Text("${stringResource(R.string.monthly_limit_label)} ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError && (limitText.toDoubleOrNull() == null || limitText.toDouble() <= 0)
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            Text(
                text = stringResource(R.string.category_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = Spacing.s)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    val isSelected = category.id == selectedCategoryId
                    val categoryColor = CategoryIconHelper.parseColorHex(category.colorHex)
                    val icon = CategoryIconHelper.getIconByName(category.iconName)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) categoryColor else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = categoryColor.copy(alpha = 0.15f),
                            selectedLabelColor = categoryColor
                        ),
                        shape = MaterialTheme.shapes.small
                    )
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
                        val limit = limitText.toDoubleOrNull()
                        if (limit != null && limit > 0 && selectedCategoryId != null) {
                            onSave(selectedCategoryId!!, limit)
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
