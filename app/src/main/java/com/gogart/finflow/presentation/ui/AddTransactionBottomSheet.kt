package com.gogart.finflow.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import com.gogart.finflow.R
import com.gogart.finflow.data.local.entity.AccountEntity
import com.gogart.finflow.data.local.entity.CategoryEntity
import com.gogart.finflow.presentation.ui.util.CategoryIconHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionBottomSheet(
    sheetState: SheetState,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, isIncome: Boolean, categoryId: Long, accountId: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var isError by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter { it.isIncome == isIncome }

    LaunchedEffect(isIncome, categories) {
        if (selectedCategoryId == null || filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = filteredCategories.firstOrNull()?.id
        }
    }

    LaunchedEffect(accounts) {
        if (selectedAccountId == null) {
            selectedAccountId = accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id
        }
    }

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
                text = stringResource(R.string.add_transaction),
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
                        selectedCategoryId = categories.firstOrNull { !it.isIncome }?.id
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (!isIncome) {
                        ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(stringResource(R.string.expense_btn), color = if (!isIncome) Color.White else Color.Gray)
                }

                Button(
                    onClick = {
                        isIncome = true
                        selectedCategoryId = categories.firstOrNull { it.isIncome }?.id
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (isIncome) {
                        ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(stringResource(R.string.income_btn), color = if (isIncome) Color.White else Color.Gray)
                }
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    isError = false
                },
                label = { Text(stringResource(R.string.title_label)) },
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
                label = { Text(stringResource(R.string.amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isError && (amountText.toDoubleOrNull() == null || amountText.toDouble() <= 0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.account_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                accounts.forEach { account ->
                    val isSelected = account.id == selectedAccountId
                    val accountColor = CategoryIconHelper.parseColorHex(account.colorHex)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAccountId = account.id },
                        label = { Text(account.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accountColor.copy(alpha = 0.15f),
                            selectedLabelColor = accountColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.category_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredCategories.forEach { category ->
                    val isSelected = category.id == selectedCategoryId
                    val categoryColor = CategoryIconHelper.parseColorHex(category.colorHex)
                    val icon = CategoryIconHelper.getIconByName(category.iconName)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = categoryColor.copy(alpha = 0.15f),
                            selectedLabelColor = categoryColor
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
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        val catId = selectedCategoryId
                        val accId = selectedAccountId
                        if (title.isNotBlank() && amount != null && amount > 0 && catId != null && accId != null) {
                            onSave(title.trim(), amount, isIncome, catId, accId)
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
