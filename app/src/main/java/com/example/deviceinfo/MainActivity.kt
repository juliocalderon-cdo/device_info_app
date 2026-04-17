package com.example.deviceinfo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Actividad principal (mínima interfaz).
 * Se encarga de solicitar permisos y disparar la primera ejecución.
 */
class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Interfaz mínima con botón de prueba
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 64, 64, 64)
            gravity = android.view.Gravity.CENTER
        }

        val textView = TextView(this).apply {
            text = "Device Info Admin\n\nEl servicio está activo y reportará automáticamente una vez al mes."
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            textSize = 18f
        }

        val button = android.widget.Button(this).apply {
            text = "Enviar Reporte Ahora (Prueba)"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 50 }
            setOnClickListener {
                triggerOrchestrator(force = true)
            }
        }

        layout.addView(textView)
        layout.addView(button)
        setContentView(layout)

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.READ_PHONE_STATE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        } else {
            // Ya tenemos permisos, intentar ejecutar
            triggerOrchestrator()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Independientemente de si se concedió o no, intentamos correr el orquestador
            // (El orquestador manejará el caso de falta de permisos devolviendo "UNKNOWN")
            triggerOrchestrator()
        }
    }

    private fun triggerOrchestrator(force: Boolean = false) {
        val orchestrator = Orchestrator(applicationContext)
        orchestrator.startReportProcess(force)
    }
}
