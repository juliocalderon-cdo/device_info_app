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
    private val API_URL = "https://script.google.com/macros/s/AKfycbwCZrXk-ej8Y53_mtQUQnsxLfM-CHy_H3G1tlhEcYIdQcg0BabM5EL0BCd_IejVKr31/exec"

    fun sendData(jsonPayload: String): Boolean {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            // NO seguir redirecciones automáticamente para POST
            connection.instanceFollowRedirects = false

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d("NetworkClient", "Código de respuesta: $responseCode")

            // Apps Script devuelve 302 (Found) cuando el POST es exitoso
            // porque intenta redirigir a una página de resultado/ejecución.
            return responseCode == HttpURLConnection.HTTP_OK || 
                   responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                   responseCode == 302
        } catch (e: Exception) {
            Log.d("NetworkClient", "Error en el envío: ${e.message}")
            return false
        } finally {
            connection?.disconnect()
        }
    }
}
