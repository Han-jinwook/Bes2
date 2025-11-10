package com.bes2.app.ui.settings

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // New launcher for the One Tap sign-in result
    val oneTapSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            viewModel.handleSignInResult(result)
        }
    )

    if (showTimePicker) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                viewModel.setSyncTime(hour, minute)
                showTimePicker = false
            },
            uiState.syncTime.hour,
            uiState.syncTime.minute,
            false // Use 24-hour format
        )
        timePickerDialog.setOnCancelListener { showTimePicker = false }
        timePickerDialog.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text("클라우드 자동 동기화", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Text("클라우드 서비스 선택", style = MaterialTheme.typography.titleMedium)
            Text("Google 포토", modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (uiState.isLoggedIn) {
                    viewModel.onLogoutClicked()
                } else {
                    scope.launch {
                        val intentSender = viewModel.beginSignIn()
                        if (intentSender != null) {
                            oneTapSignInLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        }
                    }
                }
            }) {
                Text(if (uiState.isLoggedIn) "로그아웃" else "Google 계정으로 로그인")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("자동 동기화 시간", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                Text("매일 ${uiState.syncTime.format(formatter)}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                Button(onClick = { showTimePicker = true }) {
                    Text("시간 변경")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = { viewModel.onManualSyncClicked() }, modifier = Modifier.fillMaxWidth()) {
                Text("지금 바로 동기화")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        // Preview can't have a real ViewModel, so we can't test this part in preview.
        // A more advanced preview might use a fake ViewModel implementation.
        // SettingsScreen(viewModel = // Fake ViewModel needed,
        //     navController = rememberNavController())
    }
}
