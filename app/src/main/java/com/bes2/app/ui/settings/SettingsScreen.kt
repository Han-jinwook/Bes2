package com.bes2.app.ui.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter

private const val DEBUG_TAG = "AuthFlowDebug"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            viewModel.setSyncTime(hourOfDay, minute)
            showTimePicker = false
        },
        uiState.syncTime.hour,
        uiState.syncTime.minute,
        false // 24-hour format
    )

    if (showTimePicker) {
        timePickerDialog.show()
    }


    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = viewModel::handleSignInResult
    )

    LaunchedEffect(viewModel.events) {
        launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is SettingsEvent.SyncCompleted -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                    }
                    is SettingsEvent.SyncFailed -> {
                        Toast.makeText(context, "동기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    Row(
                        modifier = Modifier.clickable { onNavigateBack() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                        Text("홈")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("클라우드 자동 동기화", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Service Selection
            SettingItem(
                title = "클라우드 서비스 선택",
                value = "Google 포토"
            ) {
                if (uiState.isLoggedIn) {
                    Button(onClick = viewModel::onLogoutClicked) {
                        Text("로그아웃")
                    }
                } else {
                    Button(onClick = {
                        scope.launch {
                            val intentSender = viewModel.beginSignIn()
                            if (intentSender != null) {
                                signInLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            } else {
                                Toast.makeText(context, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Google 계정으로 로그인")
                    }
                }
            }

            // Auto Sync Time
            val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
            SettingItem(
                title = "자동 동기화 시간",
                value = "매일 ${uiState.syncTime.format(formatter)}"
            ) {
                Button(onClick = { showTimePicker = true }) {
                    Text("시간 변경")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Manual Sync Button
            Button(
                onClick = viewModel::onManualSyncClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isLoggedIn && !uiState.isSyncing
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    Text(" 동기화 중...", modifier = Modifier.padding(start = 8.dp))
                } else {
                    Text("지금 바로 동기화")
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    value: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontSize = 18.sp)
        }
        content()
    }
    Spacer(modifier = Modifier.height(24.dp))
}
