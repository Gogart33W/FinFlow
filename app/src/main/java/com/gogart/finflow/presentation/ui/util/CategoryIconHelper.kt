package com.gogart.finflow.presentation.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            "Work" -> Icons.Default.Work
            "Laptop" -> Icons.Default.Laptop
            "TrendingUp" -> Icons.Default.TrendingUp
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "MonetizationOn" -> Icons.Default.MonetizationOn
            "AttachMoney" -> Icons.Default.AttachMoney
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Home" -> Icons.Default.Home
            "Restaurant" -> Icons.Default.Restaurant
            "SportsEsports" -> Icons.Default.SportsEsports
            "MedicalServices" -> Icons.Default.MedicalServices
            "MoreHoriz" -> Icons.Default.MoreHoriz
            else -> Icons.Default.Category
        }
    }

    fun parseColorHex(colorHex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF607D8B)
        }
    }
}
