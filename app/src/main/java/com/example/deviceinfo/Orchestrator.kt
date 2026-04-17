package com.example.deviceinfo

import android.content.Context
import android.util.Log
import com.example.deviceinfo.logic.DeviceCollector
import com.example.deviceinfo.logic.ExecutionTracker
import com.example.deviceinfo.network.NetworkClient
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * Orquestador principal de la aplicación.
 * Coordina la verificación de tiempo, recolección de datos y envío.
 */
class Orchestrator(private val context: Context) {

    private val tracker = ExecutionTracker(context)
    private val collector = DeviceCollector(context)
    private val network = NetworkClient()
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Inicia el proceso de reporte si corresponde al mes actual o si se fuerza.
     */
    fun startReportProcess(force: Boolean = false) {
        if (force || tracker.shouldExecute()) {
            Log.d("Orchestrator", "Iniciando reporte mensual...")
            
            // Ejecución en segundo plano para no bloquear el hilo principal
            executor.execute {
                try {
                    val data = collector.getDeviceInfo()
                    val json = JSONObject().apply {
                        put("modelo", data.modelo)
                        put("marca", data.marca)
                        put("version", data.version)
                        put("android_id", data.androidId)
                        put("numero_celular", data.numeroCelular)
                        put("serial", data.serial)
                        put("token", "CAMBIA_ESTE_TOKEN_SEGURO") // Debe coincidir con el GAS
                    }.toString()

                    val success = network.sendData(json)
                    
                    if (success) {
                        Log.d("Orchestrator", "Datos enviados exitosamente.")
                        tracker.markExecutionSuccess()
                    } else {
                        Log.d("Orchestrator", "Fallo al enviar datos. Se reintentará en el próximo inicio.")
                    }
                } catch (e: Exception) {
                    Log.d("Orchestrator", "Error durante el proceso: ${e.message}")
                }
            }
        } else {
            Log.d("Orchestrator", "El reporte de este mes ya fue enviado.")
        }
    }
}
