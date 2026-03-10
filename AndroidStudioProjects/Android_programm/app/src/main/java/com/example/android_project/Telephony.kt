package com.example.android_project

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrength
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Telephony : AppCompatActivity() {
    private lateinit var info: TextView
    private lateinit var buttonGet: Button
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_telephony)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        info = findViewById(R.id.infoText)
        buttonGet = findViewById(R.id.buttonGet)
        buttonBack = findViewById(R.id.buttonBack)
    }

    override fun onResume() {
        super.onResume()

        buttonBack.setOnClickListener { finish() }
        buttonGet.setOnClickListener { UpdateUi() }
    }

    private fun UpdateUi() {
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
        }

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellInfoList = telephonyManager.allCellInfo

        if (cellInfoList == null || cellInfoList.isEmpty()) {
            info.text = "Информация о сотах недоступна"
        } else {
            info.text = formatCellInfo(cellInfoList)
        }
    }


    
    private fun formatCellInfo(cellInfoList: List<CellInfo>): String {
        val sb = StringBuilder()
        sb.append("Найдено сетей: ${cellInfoList.size}\n\n")

        for ((index, cellInfo) in cellInfoList.withIndex()) {
            sb.append("=== Сеть ${index + 1} ===\n")

            when (cellInfo) {
                is CellInfoGsm -> {
                    sb.append("Тип: GSM (2G)\n")
                    sb.append("Сигнал: ${cellInfo.cellSignalStrength.dbm} dBm\n")
                }
                is CellInfoWcdma -> {
                    sb.append("Тип: WCDMA (3G)\n")
                    sb.append("Сигнал: ${cellInfo.cellSignalStrength.dbm} dBm\n")
                }
                is CellInfoLte -> {
                    sb.append("Тип: LTE (4G)\n")
                    sb.append("Сигнал: ${cellInfo.cellSignalStrength.dbm} dBm\n")
                }
                is CellInfoNr -> {
                    sb.append("Тип: NR (5G)\n")
                    sb.append("Сигнал: ${cellInfo.cellSignalStrength.dbm} dBm\n")
                }
            }

            sb.append("Активна: ${if (cellInfo.isRegistered) "Да" else "Нет"}\n")
            sb.append("\n")
        }

        return sb.toString()
    }
}