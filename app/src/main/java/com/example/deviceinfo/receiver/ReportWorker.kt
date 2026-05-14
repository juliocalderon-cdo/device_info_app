package com.example.deviceinfo.receiver

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.deviceinfo.Orchestrator
import java.util.concurrent.TimeUnit

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
            
            // Programamos la siguiente ejecución en 5 minutos
            scheduleNext(applicationContext)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("ReportWorker", "Error en tarea automática: ${e.message}")
            Result.retry()
        }
    }

    private fun scheduleNext(context: Context) {
        val nextWork = OneTimeWorkRequestBuilder<ReportWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .addTag("FiveMinuteWork")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "DailyReportWork",
            ExistingWorkPolicy.REPLACE, // Reemplazamos para asegurar la nueva programación
            nextWork
        )
    }
}
