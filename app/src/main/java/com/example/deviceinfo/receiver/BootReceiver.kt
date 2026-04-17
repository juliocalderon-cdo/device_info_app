package com.example.deviceinfo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.deviceinfo.Orchestrator

/**
 * Receptor que escucha el evento de inicio del sistema.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.d("BootReceiver", "Celular iniciado. Verificando reporte mensual...")
            val orchestrator = Orchestrator(context)
            orchestrator.startReportProcess()
        }
    }
}
