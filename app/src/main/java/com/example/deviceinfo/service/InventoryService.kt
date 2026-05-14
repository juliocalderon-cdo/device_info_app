package com.example.deviceinfo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.deviceinfo.Orchestrator

/**
 * Servicio de primer plano que mantiene la aplicación activa
 * para ejecutar el reporte cada 5 minutos de forma garantizada.
 */
class InventoryService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val INTERVAL = 5 * 60 * 1000L // 5 minutos
    private val CHANNEL_ID = "inventory_service_channel"
    private val NOTIFICATION_ID = 101

    private val reportRunnable = object : Runnable {
        override fun run() {
            Log.d("InventoryService", "Ejecutando reporte programado...")
            try {
                Orchestrator(applicationContext).startReportProcess()
            } catch (e: Exception) {
                Log.e("InventoryService", "Error en reporte: ${e.message}")
            }
            // Reprogramar para dentro de 5 minutos
            handler.postDelayed(this, INTERVAL)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        // Iniciar el ciclo de reportes
        handler.post(reportRunnable)
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Inventario Activo")
            .setContentText("El servicio de reporte automático está funcionando.")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Inventario",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(reportRunnable)
        super.onDestroy()
    }
}
