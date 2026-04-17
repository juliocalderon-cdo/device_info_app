package com.example.deviceinfo.logic

import android.content.Context
import java.util.Calendar

/**
 * Clase para verificar si la ejecución mensual ya fue realizada.
 */
class ExecutionTracker(context: Context) {

    private val prefs = context.getSharedPreferences("execution_prefs", Context.MODE_PRIVATE)

    /**
     * Compara el día actual con el guardado en SharedPreferences.
     * Retorna true si es un nuevo día o si nunca se ha ejecutado.
     */
    fun shouldExecute(): Boolean {
        val lastDay = prefs.getInt("last_day", -1)
        val lastYear = prefs.getInt("last_year", -1)

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        return (currentDay != lastDay || currentYear != lastYear)
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
