package com.bes2.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bes2.app.ui.Bes2App
import com.bes2.background.service.MediaDetectionService
import com.bes2.core_ui.theme.Bes2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bes2Theme {
                // Restore the version that moves the task to the background instead of finishing.
                Bes2App(onStartAnalysisAndExit = { moveTaskToBack(true) })
            }
        }
        startMediaDetectionService()
    }

    private fun startMediaDetectionService() {
        val serviceIntent = Intent(this, MediaDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
