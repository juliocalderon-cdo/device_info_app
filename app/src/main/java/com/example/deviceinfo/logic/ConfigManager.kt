package com.example.deviceinfo.logic

import android.os.Environment
import android.util.Log
import java.io.File

/**
 * Clase para gestionar el archivo de configuración manual en DCIM/Config/config.txt
 */
class ConfigManager {

    private val folderPath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Config")
    private val configFile = File(folderPath, "config.txt")

    /**
     * Asegura que la carpeta y el archivo existan con el formato base.
     */
    fun ensureConfigExists(): ConfigData {
        if (!folderPath.exists()) {
            folderPath.mkdirs()
        }

        if (!configFile.exists()) {
            val defaultContent = """
                USUARIO: 
                SERIAL: 
                NUMERO: 
            """.trimIndent()
            try {
                configFile.writeText(defaultContent)
            } catch (e: Exception) {
                Log.e("ConfigManager", "Error al crear archivo inicial: ${e.message}")
            }
        }
        return readConfig()
    }

    /**
     * Lee los parámetros del archivo de texto.
     */
    fun readConfig(): ConfigData {
        val data = ConfigData()
        if (!configFile.exists()) return data

        try {
            configFile.readLines().forEach { line ->
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().uppercase()
                    val value = parts[1].trim()
                    when (key) {
                        "USUARIO" -> data.usuario = value
                        "SERIAL" -> data.serial = value
                        "NUMERO" -> data.numero = value
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ConfigManager", "Error al leer config: ${e.message}")
        }
        return data
    }

    /**
     * Guarda los nuevos valores en el archivo.
     */
    fun saveConfig(usuario: String, serial: String, numero: String): Boolean {
        return try {
            if (!folderPath.exists()) folderPath.mkdirs()
            
            val content = """
                USUARIO: $usuario
                SERIAL: $serial
                NUMERO: $numero
            """.trimIndent()
            configFile.writeText(content)
            true
        } catch (e: Exception) {
            Log.e("ConfigManager", "Error al guardar config: ${e.message}")
            false
        }
    }

    data class ConfigData(
        var usuario: String = "",
        var serial: String = "",
        var numero: String = ""
    )
}
