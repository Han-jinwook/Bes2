package com.bes2.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.bes2.app.ui.Bes2App
import com.bes2.app.ui.review.ReviewActivity
import com.bes2.background.service.MediaDetectionService
import com.bes2.core_ui.theme.Bes2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // [ADDED] Hold pending navigation event
    private var pendingNavigationEvent by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        handleIntent(intent)
        
        setContent {
            Bes2Theme {
                // [MODIFIED] Pass pending event to Bes2App
                Bes2App(
                    onStartAnalysisAndExit = { moveTaskToBack(true) },
                    pendingNavigationEvent = pendingNavigationEvent
                )
            }
        }
        startMediaDetectionService()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra("NAVIGATE_TO")
        
        if (navigateTo == "REVIEW_SCREEN") {
            // ReviewActivity is separate, so we still start activity
            startActivity(Intent(this, ReviewActivity::class.java))
        } else if (navigateTo == "SCREENSHOT_CLEAN") {
            // [MODIFIED] Update state instead of restarting activity
            pendingNavigationEvent = "SCREENSHOT_CLEAN"
        }
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
