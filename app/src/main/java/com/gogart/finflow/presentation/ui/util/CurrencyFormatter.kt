package com.gogart.finflow.presentation.ui.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatAmount(amount: Double, currencySymbol: String): String {
        // Find a standard format just for numbers with 2 decimals
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        val formattedNumber = formatter.format(amount)
        
        return "$formattedNumber $currencySymbol"
    }
}
