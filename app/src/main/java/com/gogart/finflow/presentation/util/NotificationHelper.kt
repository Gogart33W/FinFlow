package com.gogart.finflow.presentation.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gogart.finflow.R

open class NotificationHelper(private val context: Context) {
    private val notificationManager: NotificationManager? =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Бюджети",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Сповіщення про перевищення бюджету"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    open fun showBudgetWarningNotification(categoryId: Long, categoryName: String, percentage: Int) {
        val title = context.getString(R.string.budget_warning)
        val message = "Витрати в категорії «$categoryName» досягли $percentage% від місячного ліміту."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Замінити на правильну іконку при наявності
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(categoryId.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "budget_alerts_channel"
    }
}
