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

    /**
     * Realiza un POST con el JSON proporcionado.
     * No bloquea el hilo principal ya que debe llamarse desde un hilo secundario.
     */
    fun sendData(jsonPayload: String): Boolean {
        var currentUrl = API_URL
        var connection: HttpURLConnection? = null
        
        try {
            // Reintentar hasta 3 veces en caso de redirección (Apps Script usa 302)
            repeat(3) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonPayload)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                Log.d("NetworkClient", "Código de respuesta: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == 307 || responseCode == 308) {
                    currentUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    Log.d("NetworkClient", "Redirigiendo a: $currentUrl")
                } else {
                    return responseCode in 200..299
                }
            }
            return false
        } catch (e: Exception) {
            Log.d("NetworkClient", "Error en el envío: ${e.message}")
            return false
        } finally {
            connection?.disconnect()
        }
    }
}
