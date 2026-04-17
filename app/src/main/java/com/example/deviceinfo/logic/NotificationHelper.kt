package com.example.deviceinfo.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Utilidad para mostrar notificaciones de estado al usuario.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "report_status_channel"
    private const val NOTIFICATION_ID = 1

    fun showStatusNotification(context: Context, success: Boolean, message: String) {
        createNotificationChannel(context)

        val title = if (success) "Reporte Exitoso" else "Error en Reporte"
        val icon = android.R.drawable.stat_sys_upload_done // Icono genérico de éxito/subida

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                // Nota: En Android 13+ esto requiere el permiso POST_NOTIFICATIONS
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Manejar falta de permisos si fuera necesario
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Estado de Reporte Mensual"
            val descriptionText = "Notificaciones sobre el envío automático de información"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
