package com.gogart.finflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.gogart.finflow.data.local.AppDataBase
import com.gogart.finflow.data.repository.TransactionRepository
import com.gogart.finflow.presentation.ui.MainScreen
import com.gogart.finflow.presentation.viewmodel.TransactionViewModel
import com.gogart.finflow.presentation.viewmodel.TransactionViewModelFactory
import com.gogart.finflow.ui.theme.FinFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TransactionViewModel by viewModels {
        val database = AppDataBase.getDatabase(applicationContext)
        val repository = TransactionRepository(database.transactionDao)
        TransactionViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinFlowTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
