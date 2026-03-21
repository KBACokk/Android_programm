package com.example.android_programm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CurrentLoc(
    val Latitute: Double,
    val Longitute: Double,
    val Altitute: Double,
    val time: Long
){
    fun saveInFile(file: File, context: Context){
        if (!file.exists()) {
            file.createNewFile()
        }
        try {
            file.appendText("""{"lat":$Latitute,"lon":$Longitute,"alt":$Altitute,"time":$time}""" + "\n")
        } catch (e: Exception) {
            Log.e("LocationData", "Ошибка при созранении в файл", e)
        }
    }
}

class LocationActivity : AppCompatActivity(), LocationListener {
    val LOG_TAG: String = "LOCATION_ACTIVITY"
    private lateinit var bBackToMain: Button
    private lateinit var buttonBack: Button

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private lateinit var locationManager: LocationManager
    private lateinit var tvLat: TextView
    private lateinit var tvLon: TextView
    private lateinit var tvAlt: TextView
    private lateinit var tvTime: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        tvLat = findViewById(R.id.lat)
        tvLon = findViewById(R.id.lon)
        tvAlt = findViewById(R.id.alt)
        tvTime = findViewById(R.id.textCurTime)
        buttonBack = findViewById(R.id.buttonBack)
    }

    override fun onResume() {
        super.onResume()

        updateCurrentLocation()
        buttonBack.setOnClickListener { finish() }

    }

    private fun updateCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }

                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    displayLocation(lastLocation)
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )

            } else {
                Toast.makeText(applicationContext, "Включите локацию в настройках", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "Разрешение не получено")
            tvLat.text = "Разрешение не выдано"
            tvLon.text = "Разрешение не выдано"
            tvAlt.text = "Разрешение не выдано"
            tvTime.text = "Разрешение не выдано"
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        Log.w(LOG_TAG, "requestPermissions()")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun checkPermissions(): Boolean{
        if( ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            return true
        } else {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Разрешение получено", Toast.LENGTH_SHORT).show()
                updateCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Отклонено пользователем", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onLocationChanged(location: Location) {
        displayLocation(location)


        val file = File(filesDir, "locations.json")
        val exfile = File("/storage/emulated/0/Documents", "locations.json")
        CurrentLoc(location.latitude, location.longitude, location.altitude, location.time).saveInFile(file, this)
        CurrentLoc(location.latitude, location.longitude, location.altitude, location.time).saveInFile(exfile, this)
    }

    private fun displayLocation(location: Location) {
        val latitude = String.format(Locale.US, "%.6f", location.latitude)
        val longitude = String.format(Locale.US, "%.6f", location.longitude)
        val altitude = String.format(Locale.US, "%.2f", location.altitude)
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        location.time
        val currentTime = dateFormat.format(Date())

        tvLat.text = "$latitude°"
        tvLon.text = "$longitude°"
        tvAlt.text = "$altitude м"
        tvTime.text = "$currentTime"
    }

    override fun onDestroy() {
        super.onDestroy()

        locationManager.removeUpdates(this)
    }
}