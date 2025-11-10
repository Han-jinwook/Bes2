package com.bes2.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bes2.app.ui.review.ReviewScreen
import com.bes2.app.ui.settings.SettingsScreen

/**
 * Main entry point for the app's UI.
 * Handles permission checks and navigation.
 * @param onStartAnalysisAndExit Callback to be invoked when the user clicks the 'Start Analysis' button.
 *                               This is expected to finish the activity, putting the app in a background 'waiting' state.
 */
@Composable
fun Bes2App(onStartAnalysisAndExit: () -> Unit) {
    val context = LocalContext.current

    // Define storage permission based on Android version
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // State for essential permissions
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasStoragePermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionsToRequest = remember {
        mutableListOf(
            Manifest.permission.CAMERA,
            storagePermission
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        hasCameraPermission = permissionsMap[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasStoragePermission = permissionsMap[storagePermission] ?: hasStoragePermission
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasStoragePermission) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Only proceed if essential permissions are granted
        if (hasCameraPermission && hasStoragePermission) {
            val navController = rememberNavController()
            AppNavigation(navController = navController, onStartAnalysisAndExit = onStartAnalysisAndExit)
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "앱을 사용하려면 카메라와 저장 공간 접근 권한을 모두 허용해야 합니다. 앱 설정에서 권한을 허용해주세요.",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AppNavigation(
    navController: NavHostController, 
    onStartAnalysisAndExit: () -> Unit
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, onStartAnalysisAndExit = onStartAnalysisAndExit)
        }
        composable("settings") {
            SettingsScreen(viewModel = hiltViewModel(), navController = navController)
        }
        composable("review") {
            ReviewScreen(viewModel = hiltViewModel(), navController = navController)
        }
    }
}

@Composable
private fun HomeScreen(
    navController: NavHostController,
    onStartAnalysisAndExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // This button now triggers the callback to exit the app UI.
        Button(
            onClick = onStartAnalysisAndExit,
            modifier = Modifier.width(200.dp)
        ) {
            Text("분석 시작")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("settings") }, modifier = Modifier.width(200.dp)) {
            Text("설정")
        }
    }
}
