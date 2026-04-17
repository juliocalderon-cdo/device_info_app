package com.example.deviceinfo.logic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log

/**
 * Clase encargada de recolectar la información técnica del dispositivo.
 */
class DeviceCollector(private val context: Context) {

    fun getDeviceInfo(): DeviceData {
        return DeviceData(
            modelo = Build.MODEL ?: "UNKNOWN",
            marca = Build.MANUFACTURER ?: "UNKNOWN",
            version = Build.VERSION.RELEASE ?: "UNKNOWN",
            androidId = getAndroidId(),
            numeroCelular = getPhoneNumber(),
            serial = getSerial()
        )
    }

    private fun getAndroidId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN"
        } catch (e: Exception) {
            Log.d("DeviceCollector", "Error al obtener Android ID: ${e.message}")
            "UNKNOWN"
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun getPhoneNumber(): String {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val number = tm?.line1Number
            if (number.isNullOrEmpty()) "UNKNOWN" else number
        } catch (e: Exception) {
            Log.d("DeviceCollector", "Error al obtener número: ${e.message}")
            "UNKNOWN"
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun getSerial(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Requiere permisos especiales en versiones modernas de Android (generalmente no disponible para apps normales)
                Build.getSerial() ?: "UNKNOWN"
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL ?: "UNKNOWN"
            }
        } catch (e: Exception) {
            Log.d("DeviceCollector", "Error al obtener Serial: ${e.message}")
            "UNKNOWN"
        }
    }

    data class DeviceData(
        val modelo: String,
        val marca: String,
        val version: String,
        val androidId: String,
        val numeroCelular: String,
        val serial: String
    )
}
