package com.example.voicechanger
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS), 101)
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val intent = Intent(this, VoiceService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, VoiceService::class.java))
        }
    }
}
