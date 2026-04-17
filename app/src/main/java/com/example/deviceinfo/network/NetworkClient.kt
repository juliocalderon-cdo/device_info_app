package com.example.deviceinfo.network

import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Clase para manejar el envío de datos via POST HTTP.
 */
class NetworkClient {

    // URL de la Web App de Google Apps Script.
    private val API_URL = "https://script.google.com/macros/s/AKfycbw6Gz5VMbVW8eR3LWCIAMJFtZMGdOwBvV4UlOuxGilAUUWTE0aToIeU7RJcRuku4CWCSQ/exec"

    /**
     * Realiza un POST con el JSON proporcionado.
     * No bloquea el hilo principal ya que debe llamarse desde un hilo secundario.
     */
    fun sendData(jsonPayload: String): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d("NetworkClient", "Código de respuesta: $responseCode")

            // Consideramos éxito cualquier código 2xx
            responseCode in 200..299
        } catch (e: Exception) {
            Log.d("NetworkClient", "Error en el envío: ${e.message}")
            false
        } finally {
            connection?.disconnect()
        }
    }
}
