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
        var activeConnection: HttpURLConnection? = null
        
        try {
            repeat(3) {
                val url = URL(currentUrl)
                val conn = url.openConnection() as HttpURLConnection
                activeConnection = conn
                
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(jsonPayload)
                    writer.flush()
                }

                val responseCode = conn.responseCode
                Log.d("NetworkClient", "Código de respuesta: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == 307 || responseCode == 308) {
                    currentUrl = conn.getHeaderField("Location")
                    conn.disconnect()
                    activeConnection = null
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
            activeConnection?.disconnect()
        }
    }
}
