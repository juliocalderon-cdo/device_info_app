package com.example.deviceinfo.logic

import android.content.Context
import java.util.Calendar

/**
 * Clase para verificar si la ejecución diaria ya fue realizada.
 */
class ExecutionTracker(context: Context) {

    private val prefs = context.getSharedPreferences("execution_prefs", Context.MODE_PRIVATE)

    /**
     * Retorna true si ha cambiado el mes o el año desde la última ejecución.
     */
    fun shouldExecute(): Boolean {
        val lastMonth = prefs.getInt("last_month", -1)
        val lastYear = prefs.getInt("last_year", -1)

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return (currentMonth != lastMonth || currentYear != lastYear)
    }

    /**
     * Guarda el mes y año actual como la última ejecución exitosa, además del timestamp.
     */
    fun markExecutionSuccess() {
        val calendar = Calendar.getInstance()
        prefs.edit().apply {
            putInt("last_month", calendar.get(Calendar.MONTH))
            putInt("last_year", calendar.get(Calendar.YEAR))
            putLong("last_time_ms", System.currentTimeMillis())
            apply()
        }
    }

    fun getLastExecutionTime(): Long {
        return prefs.getLong("last_time_ms", 0L)
    }

    fun saveError(message: String) {
        prefs.edit().putString("last_error", message).apply()
    }

    fun getLastError(): String {
        return prefs.getString("last_error", "") ?: ""
    }

    fun clearError() {
        prefs.edit().remove("last_error").apply()
    }
}
