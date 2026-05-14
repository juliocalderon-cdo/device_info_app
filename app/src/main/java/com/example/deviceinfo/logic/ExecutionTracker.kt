package com.example.deviceinfo.logic

import android.content.Context
import java.util.Calendar

/**
 * Clase para verificar si la ejecución diaria ya fue realizada.
 */
class ExecutionTracker(context: Context) {

    private val prefs = context.getSharedPreferences("execution_prefs", Context.MODE_PRIVATE)

    /**
     * Retorna true si han pasado al menos 5 minutos desde la última ejecución.
     */
    fun shouldExecute(): Boolean {
        val lastTime = getLastExecutionTime()
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMs = 5 * 60 * 1000
        
        return (currentTime - lastTime) >= fiveMinutesInMs
    }

    /**
     * Guarda el día y año actual como la última ejecución exitosa.
     */
    fun markExecutionSuccess() {
        val calendar = Calendar.getInstance()
        prefs.edit().apply {
            putInt("last_day", calendar.get(Calendar.DAY_OF_YEAR))
            putInt("last_year", calendar.get(Calendar.YEAR))
            apply()
        }
    }
}
