package com.gogart.finflow.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val INCOME_CATEGORIES = listOf("Зарплата", "Фріланс", "Інвестиції", "Подарунок", "Інше")
val EXPENSE_CATEGORIES = listOf("Продукти", "Транспорт", "Кафе та ресторани", "Розваги", "Комунальні", "Покупки", "Інше")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, isIncome: Boolean, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var isError by remember { mutableStateOf(false) }

    val categories = if (isIncome) INCOME_CATEGORIES else EXPENSE_CATEGORIES

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Нова транзакція",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Income / Expense selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        isIncome = false
                        selectedCategory = EXPENSE_CATEGORIES.first()
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (!isIncome) {
                        ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text("🔴 Витрата", color = if (!isIncome) Color.White else Color.Gray)
                }

                Button(
                    onClick = {
                        isIncome = true
                        selectedCategory = INCOME_CATEGORIES.first()
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (isIncome) {
                        ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text("🟢 Дохід", color = if (isIncome) Color.White else Color.Gray)
                }
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    isError = false
                },
                label = { Text("Назва (напр. Продукти в Сільпо)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError && title.isBlank()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        amountText = it
                        isError = false
                    }
                },
                label = { Text("Сума (₴)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError && (amountText.toDoubleOrNull() == null || amountText.toDouble() <= 0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Категорія:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            selectedLabelColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save and Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Скасувати")
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (title.isNotBlank() && amount != null && amount > 0) {
                            onSave(title.trim(), amount, isIncome, selectedCategory)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Зберегти")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
