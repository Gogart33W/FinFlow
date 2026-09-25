package com.gogart.finflow.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gogart.finflow.R
import com.gogart.finflow.data.local.entity.CategoryExpenseSummary
import com.gogart.finflow.presentation.ui.util.CategoryIconHelper
import com.gogart.finflow.presentation.ui.util.CurrencyFormatter
import com.gogart.finflow.presentation.viewmodel.StatisticsViewModel
import com.gogart.finflow.presentation.viewmodel.TimePeriod
import com.gogart.finflow.ui.theme.FinFlowExtendedColors
import com.gogart.finflow.ui.theme.Spacing
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val totalExpenseSum = expenseCategories.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_statistics),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.m)
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))

            // Time Period Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePeriod.entries.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(period.title, style = MaterialTheme.typography.labelLarge) },
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.m))

            if (totalExpenseSum <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_expense_summary),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.m),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pie Chart Section
                    item {
                        ExpensePieChartCard(
                            totalExpense = totalExpenseSum,
                            categories = expenseCategories,
                            currencySymbol = currencySymbol
                        )
                    }

                    // Top Categories Header
                    item {
                        Text(
                            text = stringResource(R.string.expense_structure_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }

                    // Top Categories List
                    itemsIndexed(expenseCategories) { _, categorySummary ->
                        CategoryExpenseProgressCard(
                            summary = categorySummary,
                            totalExpense = totalExpenseSum,
                            currencySymbol = currencySymbol
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(Spacing.m))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensePieChartCard(
    totalExpense: Double,
    categories: List<CategoryExpenseSummary>,
    currencySymbol: String
) {
    val isDark = isSystemInDarkTheme()
    val expenseColor = if (isDark) FinFlowExtendedColors.ExpenseDark else FinFlowExtendedColors.ExpenseLight

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m + Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = 36.dp.toPx()

                    categories.forEach { category ->
                        val sweepAngle = ((category.totalAmount / totalExpense) * 360f).toFloat()
                        val color = CategoryIconHelper.parseColorHex(category.colorHex)

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.total_expense_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = CurrencyFormatter.formatAmount(totalExpense, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = expenseColor
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryExpenseProgressCard(
    summary: CategoryExpenseSummary,
    totalExpense: Double,
    currencySymbol: String
) {
    val isDark = isSystemInDarkTheme()
    val expenseColor = if (isDark) FinFlowExtendedColors.ExpenseDark else FinFlowExtendedColors.ExpenseLight

    val percentage = if (totalExpense > 0) (summary.totalAmount / totalExpense) else 0.0
    val color = CategoryIconHelper.parseColorHex(summary.colorHex)
    val icon = CategoryIconHelper.getIconByName(summary.iconName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.s + Spacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = summary.categoryName,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.s + Spacing.xs))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.categoryName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", percentage * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Text(
                    text = CurrencyFormatter.formatAmount(summary.totalAmount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = expenseColor
                )
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            LinearProgressIndicator(
                progress = { percentage.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}
