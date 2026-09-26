package com.gogart.finflow

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gogart.finflow.data.backup.BackupManager
import com.gogart.finflow.data.local.AppDataBase
import com.gogart.finflow.data.preferences.SecurityManager
import com.gogart.finflow.data.repository.AccountRepository
import com.gogart.finflow.data.repository.BudgetRepository
import com.gogart.finflow.data.repository.CategoryRepository
import com.gogart.finflow.data.repository.TransactionRepository
import com.gogart.finflow.presentation.ui.AccountsScreen
import com.gogart.finflow.presentation.ui.BudgetsScreen
import com.gogart.finflow.presentation.ui.MainScreen
import com.gogart.finflow.presentation.ui.OnboardingScreen
import com.gogart.finflow.presentation.ui.PinAuthScreen
import com.gogart.finflow.presentation.ui.SettingsScreen
import com.gogart.finflow.presentation.ui.StatisticsScreen
import com.gogart.finflow.presentation.ui.navigation.BottomNavigationBar
import com.gogart.finflow.presentation.ui.navigation.Screen
import com.gogart.finflow.presentation.util.NotificationHelper
import com.gogart.finflow.presentation.viewmodel.AccountViewModel
import com.gogart.finflow.presentation.viewmodel.AccountViewModelFactory
import com.gogart.finflow.presentation.viewmodel.AuthState
import com.gogart.finflow.presentation.viewmodel.BudgetViewModel
import com.gogart.finflow.presentation.viewmodel.BudgetViewModelFactory
import com.gogart.finflow.presentation.viewmodel.SettingsViewModel
import com.gogart.finflow.presentation.viewmodel.SettingsViewModelFactory
import com.gogart.finflow.presentation.viewmodel.StatisticsViewModel
import com.gogart.finflow.presentation.viewmodel.StatisticsViewModelFactory
import com.gogart.finflow.presentation.viewmodel.TransactionViewModel
import com.gogart.finflow.presentation.viewmodel.TransactionViewModelFactory
import com.gogart.finflow.ui.theme.FinFlowTheme

class MainActivity : ComponentActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val transactionRepository = TransactionRepository(database.transactionDao)
        val categoryRepository = CategoryRepository(database.categoryDao)
        val accountRepository = AccountRepository(database.accountDao, database.transactionDao)
        val budgetRepository = BudgetRepository(database.budgetDao)
        val notificationHelper = NotificationHelper(applicationContext)
        val securityManager = SecurityManager(applicationContext)
        TransactionViewModelFactory(transactionRepository, categoryRepository, accountRepository, budgetRepository, notificationHelper, securityManager)
    }

    private val accountViewModel: AccountViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val accountRepository = AccountRepository(database.accountDao, database.transactionDao)
        val securityManager = SecurityManager(applicationContext)
        AccountViewModelFactory(accountRepository, securityManager)
    }

    private val statisticsViewModel: StatisticsViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val transactionRepository = TransactionRepository(database.transactionDao)
        val securityManager = SecurityManager(applicationContext)
        StatisticsViewModelFactory(transactionRepository, securityManager)
    }

    private val budgetViewModel: BudgetViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val budgetRepository = BudgetRepository(database.budgetDao)
        val categoryRepository = CategoryRepository(database.categoryDao)
        val securityManager = SecurityManager(applicationContext)
        BudgetViewModelFactory(budgetRepository, categoryRepository, securityManager)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val securityManager = SecurityManager(applicationContext)
        val backupManager = BackupManager(applicationContext, database)
        SettingsViewModelFactory(securityManager, backupManager)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val dynamicColor by settingsViewModel.isDynamicColorEnabled.collectAsState()

            FinFlowTheme(
                themeMode = themeMode,
                dynamicColorEnabled = dynamicColor
            ) {
                val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsState()
                val authState by settingsViewModel.authState.collectAsState()

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        viewModel = settingsViewModel,
                        onFinished = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                } else {
                    when (authState) {
                        AuthState.Loading -> {
                            // Blank/Splash screen
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                        }
                        AuthState.NeedsPin -> {
                            PinAuthScreen(viewModel = settingsViewModel)
                        }
                        AuthState.Authenticated -> {
                            MainNavigationApp(
                                transactionViewModel = transactionViewModel,
                                accountViewModel = accountViewModel,
                                statisticsViewModel = statisticsViewModel,
                                budgetViewModel = budgetViewModel,
                                settingsViewModel = settingsViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationApp(
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    statisticsViewModel: StatisticsViewModel,
    budgetViewModel: BudgetViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Home.route) {
                MainScreen(viewModel = transactionViewModel)
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(accountViewModel = accountViewModel)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = statisticsViewModel)
            }
            composable(Screen.Budgets.route) {
                BudgetsScreen(viewModel = budgetViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
