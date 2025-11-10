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
                // The Bes2App composable now takes a lambda to finish the activity.
                Bes2App(onStartAnalysisAndExit = { finish() })
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
