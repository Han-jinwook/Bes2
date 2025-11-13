package com.bes2.app.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

private const val DEBUG_TAG = "AuthFlowDebug"

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isLoggedIn) {
            Text("로그인되었습니다.", style = typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = viewModel::onLogoutClicked) {
                Text("로그아웃")
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::onManualSyncClicked,
                enabled = !uiState.isSyncing // Disable button when syncing
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("동기화 중...")
                } else {
                    Text("수동 동기화")
                }
            }

        } else {
            Text("Google 포토에 로그인하여 선택한 사진을 백업하세요.", style = typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                Timber.tag(DEBUG_TAG).d("Sign-in button clicked.")
                launch {
                    val intentSender = viewModel.beginSignIn()
                    if (intentSender != null) {
                        Timber.tag(DEBUG_TAG).d("Intent Sender is not null, launching sign-in flow.")
                        signInLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(intentSender).build())
                    } else {
                        Timber.tag(DEBUG_TAG).w("Intent Sender is null, cannot start sign-in flow.")
                        Toast.makeText(context, "로그인에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Google 계정으로 로그인")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wi-Fi에서만 업로드")
            Switch(checked = false, onCheckedChange = { /* TODO */ })
        }
    }
}
