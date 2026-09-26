package com.gogart.finflow.presentation.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.gogart.finflow.R
import com.gogart.finflow.data.backup.BackupData
import com.gogart.finflow.presentation.viewmodel.SettingsViewModel
import com.gogart.finflow.presentation.viewmodel.ThemeMode
import com.gogart.finflow.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val isPinSet by viewModel.isPinSet.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsState()
    val backupPreview by viewModel.backupDataPreview.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { msgKey ->
            val message = when (msgKey) {
                "pin_removed" -> "PIN-код успішно видалено"
                "pin_saved" -> "PIN-код успішно встановлено"
                "invalid_pin" -> "Невірний PIN-код"
                "export_success" -> "Дані успішно експортовано"
                "export_error" -> "Помилка при експорті"
                "import_success" -> "Дані успішно імпортовано"
                "import_error" -> "Помилка при імпорті"
                "import_read_error" -> "Помилка зчитування файлу бекапу"
                else -> msgKey
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.parseImportData(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.m)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            // Appearance Section
            Text(
                text = stringResource(R.string.theme_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(Spacing.m)) {
                    
                    // Language Switcher
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(Spacing.m))
                        Text(
                            text = stringResource(R.string.language_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))
                    
                    val currentLocales = AppCompatDelegate.getApplicationLocales()
                    val sysContext = LocalContext.current
                    var currentLanguage by remember { 
                        mutableStateOf(currentLocales.toLanguageTags().let { if (it.contains("uk")) 0 else 1 })
                    }

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = currentLanguage == 0,
                            onClick = {
                                currentLanguage = 0
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("uk"))
                                (sysContext as? androidx.activity.ComponentActivity)?.recreate()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Українська", style = MaterialTheme.typography.labelLarge)
                        }
                        SegmentedButton(
                            selected = currentLanguage == 1,
                            onClick = {
                                currentLanguage = 1
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                (sysContext as? androidx.activity.ComponentActivity)?.recreate()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("English", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.l))
                    
                    // Currency Switcher
                    val currencySymbol by viewModel.currencySymbol.collectAsState()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currencySymbol,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.width(24.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.m))
                        Text(
                            text = stringResource(R.string.currency_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    val currencyOptions = listOf("₴", "$", "€", "zł")
                    val selectedCurrencyIndex = currencyOptions.indexOf(currencySymbol).takeIf { it >= 0 } ?: 0

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currencyOptions.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = selectedCurrencyIndex == index,
                                onClick = { viewModel.setCurrencySymbol(label) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = currencyOptions.size)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.l))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(Spacing.m))
                        Text(
                            text = stringResource(R.string.theme_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    val sysStr = stringResource(R.string.theme_system)
                    val lightStr = stringResource(R.string.theme_light)
                    val darkStr = stringResource(R.string.theme_dark)
                    val themeOptions = listOf(sysStr, lightStr, darkStr)
                    val selectedIndex = when (themeMode) {
                        ThemeMode.SYSTEM -> 0
                        ThemeMode.LIGHT -> 1
                        ThemeMode.DARK -> 2
                    }

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themeOptions.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = selectedIndex == index,
                                onClick = {
                                    val newMode = when (index) {
                                        1 -> ThemeMode.LIGHT
                                        2 -> ThemeMode.DARK
                                        else -> ThemeMode.SYSTEM
                                    }
                                    viewModel.setThemeMode(newMode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(Spacing.m))
                            Column {
                                Text(
                                    text = stringResource(R.string.dynamic_color_label),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.dynamic_color_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Switch(
                            checked = isDynamicColorEnabled,
                            onCheckedChange = { viewModel.setDynamicColorEnabled(it) },
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Security Section
            Text(
                text = stringResource(R.string.security_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(Spacing.m))
                        Column {
                            Text(
                                text = stringResource(R.string.pin_label),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(if (isPinSet) R.string.pin_set else R.string.pin_not_set),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPinSet) Color(0xFF388E3C) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Button(onClick = { showPinDialog = true }) {
                        Text(stringResource(if (isPinSet) R.string.pin_change else R.string.pin_setup), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Data Section
            Text(
                text = stringResource(R.string.backup_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(Spacing.m)) {
                    Button(
                        onClick = { exportLauncher.launch("finflow_backup_${System.currentTimeMillis()}.json") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.s))
                        Text(stringResource(R.string.export_data), style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(Spacing.m))

                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.s))
                        Text(stringResource(R.string.import_data), style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(Spacing.s))
                    Text(
                        text = stringResource(R.string.import_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        if (showPinDialog) {
            SetPinDialog(
                onDismiss = { showPinDialog = false },
                onSave = { pin ->
                    viewModel.setPin(pin)
                    showPinDialog = false
                },
                onRemove = if (isPinSet) {
                    {
                        viewModel.setPin(null)
                        showPinDialog = false
                    }
                } else null
            )
        }

        backupPreview?.let { data ->
            ImportConfirmDialog(
                backupData = data,
                onConfirm = { viewModel.confirmImport() },
                onCancel = { viewModel.cancelImport() }
            )
        }
    }
}

@Composable
fun ImportConfirmDialog(
    backupData: BackupData,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.import_confirm_title), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                Text(stringResource(R.string.import_confirm_desc), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Spacing.s))
                Text(stringResource(R.string.import_confirm_accounts, backupData.accounts.size), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.import_confirm_categories, backupData.categories.size), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.import_confirm_transactions, backupData.transactions.size), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.import_confirm_budgets, backupData.budgets.size), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = stringResource(R.string.import_confirm_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.import_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.security_title), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                Text(stringResource(R.string.pin_setup_desc), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Spacing.m))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            isError = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = isError,
                    label = { Text(stringResource(R.string.pin_label)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length == 4) {
                        onSave(pin)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            if (onRemove != null) {
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.remove_pin), color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}
