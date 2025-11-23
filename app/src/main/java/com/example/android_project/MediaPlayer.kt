package com.example.android_project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.util.concurrent.TimeUnit
import android.content.Context
import android.graphics.Color
import android.media.AudioManager


class MediaPlayer : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false
    private lateinit var PlayList: ListView
    private var mediaPlayer: android.media.MediaPlayer? = null
    private val trackNames = mutableListOf<String>()
    private val trackPaths = mutableListOf<String>()

    private lateinit var nameTitle: TextView

    private lateinit var musicButton: ImageButton

    private lateinit var musicSlider: SeekBar
    private lateinit var volumeSlider: SeekBar
    private lateinit var currentTime: TextView
    private lateinit var fullTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_player)

        PlayList = findViewById(R.id.tracksListView)
        nameTitle = findViewById(R.id.currentTrackTitle)
        musicButton = findViewById(R.id.btnPlayPause)
        musicSlider = findViewById(R.id.seekBar)
        volumeSlider = findViewById(R.id.volume)

        currentTime = findViewById(R.id.CurrentTime)
        fullTime = findViewById(R.id.TotalTime)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBar.left,
                systemBar.top,
                systemBar.right,
                systemBar.bottom)
            insets
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                trackList()
            }
            else {
                Toast.makeText(this, "Для работы приложения требуется разрешение", Toast.LENGTH_LONG).show()
            }
        }
        requestPermissionLauncher.launch(READ_MEDIA_AUDIO)


        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val tempMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val tempCurrntVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeSlider.progress = tempCurrntVolume
        volumeSlider.max = tempMaxVolume

        volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        musicButton.setOnClickListener {
            when {
                mediaPlayer == null-> {
                    Toast.makeText(this, "Выберите музыку", Toast.LENGTH_SHORT).show()
                }
                mediaPlayer!!.isPlaying -> {
                    mediaPlayer!!.pause()
                    musicButton.setImageResource(R.drawable.play_icon)
                }
                else -> {
                    mediaPlayer!!.start()
                    musicButton.setImageResource(R.drawable.pause_icon)
                    updateMusicSlider()
                }
            }
        }
        musicSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentTime.text = toTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })
    }
    private fun getFiles (folder : File) {
        trackNames.clear()
        trackPaths.clear()
        if (folder.exists() && folder.isDirectory){
            folder.listFiles()?.forEach { file ->
                if (file.isFile) {
                    trackNames.add(file.name)
                    trackPaths.add(file.absolutePath)
                }
            }
        }}

    private fun updateMusicSlider() {
        mediaPlayer?.let { player ->
            if (player.isPlaying && !isUserSeeking) {
                musicSlider.progress = player.currentPosition
                currentTime.text = toTime(player.currentPosition.toLong())
                handler.postDelayed({ updateMusicSlider() }, 500)
            }
        }
    }

    private fun toTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun playMusic (path: String) {
        mediaPlayer?.stop()
        mediaPlayer?.release()

        mediaPlayer = android.media.MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
        musicSlider.max = mediaPlayer!!.duration
        fullTime.text = toTime(mediaPlayer!!.duration.toLong())
        currentTime.text = toTime(0)
        nameTitle.text = File(path).nameWithoutExtension

        musicButton.setImageResource(R.drawable.pause_icon)
        updateMusicSlider()
    }


    private fun trackList () {
        val folder = File("/storage/emulated/0/Music")
        getFiles(folder)

        if (trackNames.isEmpty()) {
            Toast.makeText(this, "Папкаа Music пуста", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.item_track,
            android.R.id.text1,
            trackNames
        )

        PlayList.adapter = adapter

        PlayList.setOnItemClickListener { parent, view, position, id ->
            val trackPath = trackPaths[position]
            playMusic(trackPath)
        }
    }
    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}