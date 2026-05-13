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
import com.bumptech.glide.Glide
import com.example.deviceinfo.logic.ConfigManager

/**
 * Actividad principal.
 * Maneja la configuración manual, permisos y disparar reportes.
 */
class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100
    private val configManager = ConfigManager()
    private val LOGO_URL = "https://www.grupovierci.com/wp-content/uploads/2025/11/10paises_BLANCO-1024x640.png"

    private lateinit var editUsuario: EditText
    private lateinit var editSerial: EditText
    private lateinit var editNumero: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupUI()
        checkAndRequestPermissions()
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
            text = "Configuración de Inventario"
            textSize = 24f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 32)
        }

        // Campos de configuración
        editUsuario = createField(layout, "Usuario:")
        editSerial = createField(layout, "Serial Manual:")
        editNumero = createField(layout, "Número Manual:")

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
        
        setContentView(layout)
        loadCurrentConfig()
    }

    private fun createField(container: LinearLayout, label: String): EditText {
        container.addView(TextView(this).apply { 
            text = label 
            setTextColor(Color.WHITE)
        })
        val editText = EditText(this).apply {
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
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
        // Eliminado loadCurrentConfig() para no sobreescribir lo que el usuario escribe al minimizar
    }

    private fun triggerOrchestrator(force: Boolean = false) {
        Orchestrator(applicationContext).startReportProcess(force)
    }
}
