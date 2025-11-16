package com.bes2.app.ui.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bes2.app.ui.settings.SettingsEvent
import com.navercorp.nid.NaverIdLoginSDK
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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

    if (showTimePicker) {
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
        timePickerDialog.setOnCancelListener { showTimePicker = false }
        timePickerDialog.show()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = viewModel::handleGoogleSignInResult
    )

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.SyncCompleted -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                is SettingsEvent.SyncFailed -> Toast.makeText(context, "동기화에 실패했습니다.", Toast.LENGTH_SHORT).show()
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            modifier = Modifier.padding(start = 8.dp)
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
            Spacer(modifier = Modifier.height(24.dp))

            CloudProviderSelection(
                selectedProvider = uiState.selectedProvider,
                isLoggedIn = uiState.isLoggedIn,
                onProviderSelected = viewModel::onProviderSelected,
                onGoogleLoginClick = {
                    scope.launch {
                        val intentSender = viewModel.beginGoogleSignIn()
                        if (intentSender != null) {
                            googleSignInLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        } else {
                            Toast.makeText(context, "Google 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onNaverLoginClick = {
                    val naverLoginCallback = viewModel.getNaverLoginCallback()
                    NaverIdLoginSDK.authenticate(context, naverLoginCallback)
                },
                onLogoutClick = viewModel::onLogoutClicked
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
            SettingItem(
                title = "자동 동기화 시간",
                value = "매일 ${uiState.syncTime.format(formatter)}",
                content = {
                    Button(onClick = { showTimePicker = true }) { Text("시간 변경") }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingRow(
                title = "Wi-Fi에서만 업로드",
                content = {
                    Switch(
                        checked = uiState.uploadOnWifiOnly,
                        onCheckedChange = viewModel::onUploadOnWifiOnlyChanged
                    )
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::onManualSyncClicked,
                modifier = Modifier.fillMaxWidth().height(48.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloudProviderSelection(
    selectedProvider: String,
    isLoggedIn: Boolean,
    onProviderSelected: (String) -> Unit,
    onGoogleLoginClick: () -> Unit,
    onNaverLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val providers = mapOf("google_photos" to "Google 포토", "naver_mybox" to "Naver MyBox")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("클라우드 서비스 선택", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = providers[selectedProvider] ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                providers.forEach { (key, value) ->
                    DropdownMenuItem(
                        text = { Text(value) },
                        onClick = {
                            onProviderSelected(key)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if(isLoggedIn) {
            Button(onClick = onLogoutClick) { Text("로그아웃") }
        } else {
            when(selectedProvider) {
                "google_photos" -> Button(onClick = onGoogleLoginClick) { Text("Google 계정으로 로그인") }
                "naver_mybox" -> Button(onClick = onNaverLoginClick) { Text("Naver 계정으로 로그인") }
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
}

@Composable
fun SettingRow(
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 18.sp)
        content()
    }
}
