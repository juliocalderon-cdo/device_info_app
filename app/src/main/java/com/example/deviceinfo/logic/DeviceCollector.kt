package com.example.deviceinfo.logic

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import java.net.NetworkInterface
import java.util.Collections

/**
 * Clase encargada de recolectar la información técnica del dispositivo.
 */
class DeviceCollector(private val context: Context) {

    fun getDeviceInfo(): DeviceData {
        val data = DeviceData(
            modelo = Build.MODEL ?: "UNKNOWN",
            marca = Build.MANUFACTURER ?: "UNKNOWN",
            version = Build.VERSION.RELEASE ?: "UNKNOWN",
            androidId = getAndroidId(),
            numeroCelular = getPhoneNumber(),
            serial = getSerial(),
            macAddress = getMacAddress()
        )
        return data
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
            // Intentar con SubscriptionManager en Android 11+ (API 30+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeInfos = sm?.activeSubscriptionInfoList
                if (!activeInfos.isNullOrEmpty()) {
                    // Nota: getNumber() suele retornar vacío si la SIM no lo tiene grabado
                    val number = activeInfos[0].number
                    if (!number.isNullOrEmpty()) return number
                }
            }
            
            // Fallback al método tradicional
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val number = tm?.line1Number
            if (number.isNullOrEmpty()) "UNKNOWN" else number
        } catch (e: Exception) {
            Log.d("DeviceCollector", "Error al obtener número: ${e.message}")
            "UNKNOWN"
        }
    }

    /**
     * Intenta obtener la MAC address física usando NetworkInterface.
     * En Android 6.0+ WifiInfo.getMacAddress() devuelve 02:00:00:00:00:00.
     * Este método intenta leer directamente las interfaces del sistema.
     */
    private fun getMacAddress(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return "02:00:00:00:00:00"
                val res = StringBuilder()
                for (b in macBytes) {
                    res.append(String.format("%02X:", b))
                }

                if (res.isNotEmpty()) {
                    res.deleteCharAt(res.length - 1)
                }
                return res.toString()
            }
        } catch (e: Exception) {
            Log.d("DeviceCollector", "Error al obtener MAC: ${e.message}")
        }
        return "02:00:00:00:00:00"
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun getSerial(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Requiere permisos especiales en versiones modernas de Android
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
        val serial: String,
        val macAddress: String
    )
}
