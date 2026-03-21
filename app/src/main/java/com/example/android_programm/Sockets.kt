package com.example.android_programm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Sockets : AppCompatActivity() {

    private lateinit var tv: TextView
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sockets)

        tv = findViewById(R.id.tvSockets)
        val btn = findViewById<Button>(R.id.btnStartZmq)

        btn.setOnClickListener {
            if (!isServiceRunning) {
                checkPermissionsAndStart()
                btn.text = "Остановить мониторинг"
            } else {
                stopMonitoring()
                btn.text = "Запустить мониторинг"
            }
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.FOREGROUND_SERVICE
        )

        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            startMonitoring()
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    private fun startMonitoring() {
        val intent = Intent(this, BackgroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
        isServiceRunning = true
        tv.text = "Статус: Работает в фоновом режиме"
    }

    private fun stopMonitoring() {
        stopService(Intent(this, BackgroundService::class.java))
        isServiceRunning = false
        tv.text = "Статус: Остановлен"
    }
}