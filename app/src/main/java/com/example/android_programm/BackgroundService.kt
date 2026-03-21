package com.example.android_programm

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.*
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.concurrent.Executors
import java.io.File

class BackgroundService : Service(), LocationListener {

    private val CHANNEL_ID = "NetMonitoringChannel"
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private var zmqContext: ZContext? = null

    private var lastLocation: Location? = null
    private var isScheduled = false

    private val serverAddress = "tcp://192.168.0.174:7777"

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        zmqContext = ZContext()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Мониторинг запущен")
            .setContentText("Отправка данных каждые 7.5 секунд")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this)
        }

        startPeriodicSending()

        return START_STICKY
    }

    private fun startPeriodicSending() {
        if (!isScheduled) {
            isScheduled = true
            handler.postDelayed(object : Runnable {
                override fun run() {
                    sendDataIfLocationAvailable()
                    handler.postDelayed(this, 7500)
                }
            }, 7500)
        }
    }

    private fun sendDataIfLocationAvailable() {
        lastLocation?.let { location ->
            executor.execute {
                try {
                    val json = JSONObject().apply {
                        put("lat", location.latitude)
                        put("lon", location.longitude)
                        put("accuracy", location.accuracy)
                        put("timestamp", System.currentTimeMillis())
                        put("networkType", getNetworkTypeName(telephonyManager.networkType))
                        put("cells", getCellsData())
                    }.toString()

                    saveToFile(json)

                    sendZmqData(json)

                } catch (e: Exception) {
                    Log.e("DataError", "Ошибка формирования данных: ${e.message}")
                    e.printStackTrace()
                }
            }
        } ?: run {
            Log.d("Location", "Нет данных о местоположении, ожидание...")
        }
    }

    private fun sendZmqData(data: String) {
        try {
            zmqContext?.let { context ->

                val socket = context.createSocket(SocketType.REQ)
                socket.linger = 0
                socket.receiveTimeOut = 4000
                socket.sendTimeOut = 4000

                Log.d("ZMQ", "Подключение к $serverAddress")
                socket.connect(serverAddress)

                val dataBytes = data.toByteArray(ZMQ.CHARSET)
                Log.d("ZMQ", "Отправка ${dataBytes.size} байт")

                val sent = socket.send(dataBytes, 0)
                if (sent) {
                    Log.d("ZMQ", "Данные отправлены, ожидание ответа...")

                    val reply = socket.recv(0)
                    if (reply != null) {
                        val replyStr = String(reply, ZMQ.CHARSET)
                        Log.d("ZMQ", "Получен ответ от сервера: $replyStr")
                    } else {
                        Log.e("ZMQ", "Нет ответа от сервера (таймаут)")
                    }
                } else {
                    Log.e("ZMQ", "Не удалось отправить данные")
                }

                socket.close()
            }
        } catch (e: Exception) {
            Log.e("ZMQ", "Send error: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        Log.d("Location", "Обновлена локация: ${location.latitude}, ${location.longitude}, точность: ${location.accuracy}")
    }

    private fun saveToFile(data: String) {
        try {
            val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

            if (dir != null && !dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, "network_monitoring_data.json")

            Log.d("FileSave", "Сохранение в файл: ${file.absolutePath}")

            val timestampedData = "[${System.currentTimeMillis()}] $data"
            file.appendText(timestampedData + "\n")

        } catch (e: Exception) {
            Log.e("FileError", "Ошибка сохранения в файл: ${e.message}")
        }
    }

    private fun getNetworkTypeName(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G/LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "Unknown"
        }
    }

    private fun getCellsData() = JSONArray().apply {
        if (ActivityCompat.checkSelfPermission(this@BackgroundService, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.allCellInfo?.forEach { info ->
                val cellJson = JSONObject()
                cellJson.put("registered", info.isRegistered)

                when (info) {
                    is CellInfoLte -> {
                        cellJson.put("type", "LTE")
                        val id = info.cellIdentity
                        val sig = info.cellSignalStrength

                        cellJson.put("mcc", id.mccString ?: "")
                        cellJson.put("mnc", id.mncString ?: "")
                        cellJson.put("pci", id.pci)
                        cellJson.put("tac", id.tac)

                        cellJson.put("dbm", sig.dbm)
                        cellJson.put("asu", sig.asuLevel)
                        cellJson.put("cqi", sig.cqi)
                        cellJson.put("rsrp", sig.rsrp)
                        cellJson.put("rsrq", sig.rsrq)
                        cellJson.put("rssi", sig.rssi)
                        cellJson.put("rssnr", sig.rssnr)
                        cellJson.put("ta", sig.timingAdvance)
                    }

                    is CellInfoGsm -> {
                        cellJson.put("type", "GSM")
                        val id = info.cellIdentity
                        val sig = info.cellSignalStrength

                        cellJson.put("bsic", id.bsic)
                        cellJson.put("arfcn", id.arfcn)
                        cellJson.put("lac", id.lac)
                        cellJson.put("mcc", id.mccString ?: "")
                        cellJson.put("mnc", id.mncString ?: "")
                        cellJson.put("dbm", sig.dbm)
                        cellJson.put("rssi", sig.rssi)
                        cellJson.put("ta", sig.timingAdvance)
                    }

                    is CellInfoNr -> {
                        cellJson.put("type", "NR")
                        val id = info.cellIdentity as CellIdentityNr
                        val sig = info.cellSignalStrength as CellSignalStrengthNr

                        cellJson.put("mcc", id.mccString ?: "")
                        cellJson.put("mnc", id.mncString ?: "")
                        cellJson.put("nci", id.nci)
                        cellJson.put("pci", id.pci)
                        cellJson.put("tac", id.tac)
                        cellJson.put("nrarfcn", id.nrarfcn)
                        cellJson.put("ss_rsrp", sig.ssRsrp)
                        cellJson.put("ss_rsrq", sig.ssRsrq)
                        cellJson.put("ss_sinr", sig.ssSinr)
                    }

                    else -> cellJson.put("type", "Unknown")
                }
                put(cellJson)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Monitoring", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(chan)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        locationManager.removeUpdates(this)
        handler.removeCallbacksAndMessages(null)
        zmqContext?.close()
        executor.shutdown()
        super.onDestroy()
    }
}