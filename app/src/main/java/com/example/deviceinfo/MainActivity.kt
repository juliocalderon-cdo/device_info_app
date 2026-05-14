package com.example.deviceinfo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.graphics.Color
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.bumptech.glide.Glide
import com.example.deviceinfo.logic.ConfigManager
import com.example.deviceinfo.receiver.ReportWorker
import com.example.deviceinfo.service.InventoryService
import java.util.concurrent.TimeUnit

/**
 * Actividad principal.
 * Maneja la configuración manual, permisos y disparar reportes.
 */
class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100
    private val configManager = ConfigManager()
    private val LOGO_URL = "https://www.grupovierci.com/wp-content/uploads/2025/11/10paises_BLANCO-1024x640.png"
    private val tracker by lazy { com.example.deviceinfo.logic.ExecutionTracker(this) }

    private lateinit var editUsuario: EditText
    private lateinit var editSerial: EditText
    private lateinit var editNumero: EditText
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupUI()
        // Iniciamos el servicio de primer plano para ejecución garantizada
        startInventoryService()
        checkAndRequestPermissions()
        updateStatusUI()
    }

    private fun startInventoryService() {
        val intent = Intent(this, InventoryService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }


    private fun setupUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 48, 64, 48)
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#0a0f1e"))
        }

        // Logo
        val logoImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(600, 400).apply {
                bottomMargin = 32
            }
        }
        Glide.with(this).load(LOGO_URL).into(logoImage)

        val title = TextView(this).apply {
            text = "Inventario de Dispositivos"
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }

        // Campos de configuración
        editUsuario = createField(layout, "Usuario:")
        editSerial = createField(layout, "Número de serie del dispositivo:")
        editNumero = createField(layout, "Número de celular:")

        val btnSave = Button(this).apply {
            text = "Guardar Configuración"
            setOnClickListener { saveManualConfig() }
        }

        val btnSend = Button(this).apply {
            text = "Enviar Reporte Ahora"
            setOnClickListener { triggerOrchestrator(force = true) }
        }

        layout.addView(logoImage)
        layout.addView(title)
        layout.addView(btnSave)
        layout.addView(TextView(this).apply { text = "\n" })
        layout.addView(btnSend)

        txtStatus = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 48, 0, 0)
            text = "Consultando programador..."
        }
        layout.addView(txtStatus)
        
        setContentView(layout)
        loadCurrentConfig()
    }

    private fun createField(container: LinearLayout, label: String): EditText {
        container.addView(TextView(this).apply { 
            text = label 
            setTextColor(Color.WHITE)
            setPadding(0, 16, 0, 8)
        })
        val editText = EditText(this).apply {
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#E0E0E0")) // Color gris claro para visibilidad
            setPadding(24, 16, 24, 16)
            setHintTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        container.addView(editText)
        return editText
    }

    private fun loadCurrentConfig() {
        if (hasFullStoragePermission()) {
            val config = configManager.ensureConfigExists()
            editUsuario.setText(config.usuario)
            editSerial.setText(config.serial)
            editNumero.setText(config.numero)
        }
    }

    private fun saveManualConfig() {
        if (!hasFullStoragePermission()) {
            Toast.makeText(this, "Primero otorga permiso de archivos", Toast.LENGTH_LONG).show()
            requestFullStoragePermission()
            return
        }

        val usuario = editUsuario.text.toString().trim()
        val serial = editSerial.text.toString().trim()
        val numero = editNumero.text.toString().trim()

        if (usuario.isEmpty() || serial.isEmpty() || numero.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show()
            return
        }

        val success = configManager.saveConfig(usuario, serial, numero)

        if (success) {
            Toast.makeText(this, "Configuración guardada en DCIM/Config", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasFullStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestFullStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.READ_PHONE_STATE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
        
        if (!hasFullStoragePermission()) {
            requestFullStoragePermission()
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusUI()
    }

    private fun updateStatusUI() {
        val lastTime = tracker.getLastExecutionTime()
        val currentTime = System.currentTimeMillis()
        val interval = 3 * 60 * 60 * 1000L // 3 horas
        
        if (lastTime == 0L) {
            txtStatus.text = "Servicio de Inventario: ACTIVO\nPróximo envío: Programado cada 3 horas"
        } else {
            val nextTime = lastTime + interval
            val diff = nextTime - currentTime
            
            if (diff <= 0) {
                txtStatus.text = "Servicio de Inventario: ACTIVO\nPróximo envío: Iniciando..."
            } else {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                val minutes = (TimeUnit.MILLISECONDS.toMinutes(diff) % 60)
                txtStatus.text = "Servicio de Inventario: ACTIVO\nPróximo envío en: $hours h y $minutes min"
            }
        }
    }

    private fun triggerOrchestrator(force: Boolean = false) {
        val usuario = editUsuario.text.toString().trim()
        val serial = editSerial.text.toString().trim()
        val numero = editNumero.text.toString().trim()

        if (usuario.isEmpty() || serial.isEmpty() || numero.isEmpty()) {
            Toast.makeText(this, "Debe completar y guardar la configuración antes de enviar", Toast.LENGTH_LONG).show()
            return
        }
        
        Orchestrator(applicationContext).startReportProcess(force)
    }
}
