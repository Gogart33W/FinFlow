package com.gogart.finflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gogart.finflow.data.local.AppDataBase
import com.gogart.finflow.data.repository.AccountRepository
import com.gogart.finflow.data.repository.BudgetRepository
import com.gogart.finflow.data.repository.CategoryRepository
import com.gogart.finflow.data.repository.TransactionRepository
import com.gogart.finflow.presentation.ui.AccountsScreen
import com.gogart.finflow.presentation.ui.BudgetsScreen
import com.gogart.finflow.presentation.ui.MainScreen
import com.gogart.finflow.presentation.ui.StatisticsScreen
import com.gogart.finflow.presentation.ui.navigation.BottomNavigationBar
import com.gogart.finflow.presentation.ui.navigation.Screen
import com.gogart.finflow.presentation.util.NotificationHelper
import com.gogart.finflow.presentation.viewmodel.AccountViewModel
import com.gogart.finflow.presentation.viewmodel.AccountViewModelFactory
import com.gogart.finflow.presentation.viewmodel.BudgetViewModel
import com.gogart.finflow.presentation.viewmodel.BudgetViewModelFactory
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
        TransactionViewModelFactory(transactionRepository, categoryRepository, accountRepository, budgetRepository, notificationHelper)
    }

    private val accountViewModel: AccountViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val accountRepository = AccountRepository(database.accountDao, database.transactionDao)
        AccountViewModelFactory(accountRepository)
    }

    private val statisticsViewModel: StatisticsViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val transactionRepository = TransactionRepository(database.transactionDao)
        StatisticsViewModelFactory(transactionRepository)
    }

    private val budgetViewModel: BudgetViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val budgetRepository = BudgetRepository(database.budgetDao)
        val categoryRepository = CategoryRepository(database.categoryDao)
        BudgetViewModelFactory(budgetRepository, categoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinFlowTheme {
                MainNavigationApp(
                    transactionViewModel = transactionViewModel,
                    accountViewModel = accountViewModel,
                    statisticsViewModel = statisticsViewModel,
                    budgetViewModel = budgetViewModel
                )
            }
        }
    }
}

@Composable
fun MainNavigationApp(
    transactionViewModel: TransactionViewModel,
    accountViewModel: AccountViewModel,
    statisticsViewModel: StatisticsViewModel,
    budgetViewModel: BudgetViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
        }
    }
}
