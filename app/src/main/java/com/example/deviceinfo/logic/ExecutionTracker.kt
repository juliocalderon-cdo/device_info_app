package com.example.deviceinfo.logic

import android.content.Context
import java.util.Calendar

/**
 * Clase para verificar si la ejecución mensual ya fue realizada.
 */
class ExecutionTracker(context: Context) {

    private val prefs = context.getSharedPreferences("execution_prefs", Context.MODE_PRIVATE)

    /**
     * Compara el mes y año actual con el guardado en SharedPreferences.
     * Retorna true si es un nuevo mes o si nunca se ha ejecutado.
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
     * Guarda el mes y año actual como la última ejecución exitosa.
     */
    fun markExecutionSuccess() {
        val calendar = Calendar.getInstance()
        prefs.edit().apply {
            putInt("last_month", calendar.get(Calendar.MONTH))
            putInt("last_year", calendar.get(Calendar.YEAR))
            apply()
        }
    }
}
