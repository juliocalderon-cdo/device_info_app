package com.example.deviceinfo.receiver

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.deviceinfo.Orchestrator

/**
2:  * Trabajador que ejecuta el reporte de inventario en segundo plano.
3:  * Programado por WorkManager para ejecutarse periódicamente.
4:  */
class ReportWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("ReportWorker", "Iniciando tarea automática de reporte...")
        
        return try {
            // Ejecutamos el proceso de reporte
            val orchestrator = Orchestrator(applicationContext)
            orchestrator.startReportProcess()
            
            Result.success()
        } catch (e: Exception) {
            Log.e("ReportWorker", "Error en tarea automática: ${e.message}")
            Result.retry()
        }
    }
}
