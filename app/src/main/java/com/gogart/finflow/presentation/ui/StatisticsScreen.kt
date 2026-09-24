package com.gogart.finflow.presentation.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gogart.finflow.R
import com.gogart.finflow.data.local.entity.CategoryExpenseSummary
import com.gogart.finflow.presentation.ui.util.CategoryIconHelper
import com.gogart.finflow.presentation.viewmodel.StatisticsViewModel
import com.gogart.finflow.presentation.viewmodel.TimePeriod
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val periodSummary by viewModel.periodSummary.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    val totalExpenseSum = expenseCategories.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_statistics),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Time Period Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePeriod.entries.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(period.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalExpenseSum <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "За обраний період витрат не знайдено.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pie Chart Section
                    item {
                        ExpensePieChartCard(
                            totalExpense = totalExpenseSum,
                            categories = expenseCategories
                        )
                    }

                    // Top Categories Header
                    item {
                        Text(
                            text = "Структура витрат за категоріями",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Top Categories List
                    itemsIndexed(expenseCategories) { index, categorySummary ->
                        CategoryExpenseProgressCard(
                            rank = index + 1,
                            summary = categorySummary,
                            totalExpense = totalExpenseSum
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensePieChartCard(
    totalExpense: Double,
    categories: List<CategoryExpenseSummary>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                        text = "Всього витрат",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f ₴", totalExpense),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryExpenseProgressCard(
    rank: Int,
    summary: CategoryExpenseSummary,
    totalExpense: Double
) {
    val percentage = if (totalExpense > 0) (summary.totalAmount / totalExpense) else 0.0
    val color = CategoryIconHelper.parseColorHex(summary.colorHex)
    val icon = CategoryIconHelper.getIconByName(summary.iconName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%% від усіх витрат", percentage * 100),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = String.format(Locale.getDefault(), "%.2f ₴", summary.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { percentage.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
        }
    }
}
