package com.example.android_project

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.zeromq.SocketType
import org.zeromq.ZContext

class Sockets : AppCompatActivity() {
    private lateinit var tvSockets: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private val handler = Handler(Looper.getMainLooper())
    private val serverIp = "192.168.3.25"
    private val serverPort = 7777
    private val PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sockets)

        tvSockets = findViewById(R.id.tvSockets)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        findViewById<Button>(R.id.btnStartZmq).setOnClickListener { checkPermissions() }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )

        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            getLocation()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                getLocation()
            } else {
                tvSockets.text = "Ошибка: Нет разрешений"
                Toast.makeText(this, "Нужны разрешения!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLocation() {
        tvSockets.text = "Получаю GPS..."
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { sendToServer(it) }
                ?: run { tvSockets.text = "Нет данных GPS. Включите GPS" }
        } catch (e: Exception) {
            tvSockets.text = "Ошибка: ${e.message}"
        }
    }

    private fun getSignalStrength(): Int {
        return try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return 0
            telephonyManager.allCellInfo?.firstOrNull()?.let { info ->
                when (info) {
                    is android.telephony.CellInfoGsm -> info.cellSignalStrength.dbm
                    is android.telephony.CellInfoLte -> info.cellSignalStrength.dbm
                    is android.telephony.CellInfoWcdma -> info.cellSignalStrength.dbm
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) { 0 }
    }

    private fun getNetworkType(): String {
        return try {
            when (telephonyManager.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
                else -> "Другая"
            }
        } catch (e: Exception) { "Неизвестно" }
    }

    private fun sendToServer(location: Location) {
        val jsonString = JSONArray().apply {
            put(location.latitude)
            put(location.longitude)
            put(location.accuracy)
            put(System.currentTimeMillis())
            put(getSignalStrength())
            put(getNetworkType())
        }.toString()

        val displayText = """
            lat: ${location.latitude}
            lon: ${location.longitude}
            accuracy: ${location.accuracy} м
            signal: ${getSignalStrength()} dBm
            net: ${getNetworkType()}
        """.trimIndent()

        runOnUiThread { tvSockets.text = displayText }

        Thread {
            try {
                ZContext().use { context ->
                    val socket = context.createSocket(SocketType.REQ)
                    socket.connect("tcp://$serverIp:$serverPort")
                    socket.send(jsonString)
                    socket.recv(5000)?.let { response ->
                        runOnUiThread { tvSockets.text = "$displayText\n\nОтвет:\n${String(response)}" }
                    } ?: runOnUiThread { tvSockets.text = "$displayText\n\nНет ответа" }
                }
            } catch (e: Exception) {
                runOnUiThread { tvSockets.text = "$displayText\n\nОшибка: ${e.message}" }
            }
        }.start()
    }
}