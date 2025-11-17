package com.bes2.app.ui.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDailyTimePicker by remember { mutableStateOf(false) }
    var showDelayTimePicker by remember { mutableStateOf(false) }

    if (showDailyTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.setSyncTime(hourOfDay, minute)
                showDailyTimePicker = false
            },
            uiState.syncTime.hour,
            uiState.syncTime.minute,
            false // AM/PM format
        ).apply { setOnCancelListener { showDailyTimePicker = false }; show() }
    }

    if (showDelayTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.setSyncDelay(hourOfDay, minute)
                showDelayTimePicker = false
            },
            uiState.syncDelayHours,
            uiState.syncDelayMinutes,
            true // 24-hour format for duration
        ).apply {
            setTitle("지연 시간 설정 (시/분)")
            setOnCancelListener { showDelayTimePicker = false }
            show()
        }
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
                title = { },
                navigationIcon = {
                    Row(
                        modifier = Modifier.clickable { onNavigateBack() }.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "홈으로",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("클라우드 자동 동기화", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

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
            
            UnifiedSyncOptions(
                uiState = uiState,
                onOptionSelected = viewModel::onSyncOptionChanged,
                onDailyTimeClick = { showDailyTimePicker = true },
                onDelayTimeClick = { showDelayTimePicker = true }
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
            
            Spacer(modifier = Modifier.weight(1f, fill = true))

            Button(
                onClick = viewModel::onManualSyncClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState.isLoggedIn && !uiState.isSyncing
            ) {
                if (uiState.isSyncing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("동기화 중...")
                    }
                } else {
                    Text("지금 바로 동기화")
                }
            }
        }
    }
}

@Composable
fun UnifiedSyncOptions(
    uiState: SettingsUiState,
    onOptionSelected: (String) -> Unit,
    onDailyTimeClick: () -> Unit,
    onDelayTimeClick: () -> Unit
) {
    val options = listOf(
        "DAILY" to "매일 설정시간 마다 동기화",
        "IMMEDIATE" to "분석 직후 바로바로 동기화",
        "DELAYED" to "분석 직후 설정 시간 이후 동기화",
        "NONE" to "자동 동기화 안함"
    )

    Column {
        options.forEach { (key, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (key == uiState.syncOption),
                        onClick = { onOptionSelected(key) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (key == uiState.syncOption),
                    onClick = { onOptionSelected(key) }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text)
                    if (key == "DAILY" && uiState.syncOption == "DAILY") {
                        val formatter = remember { DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN) }
                        TextButton(onClick = onDailyTimeClick, contentPadding = PaddingValues(start = 0.dp)) {
                            Text("매일 ${uiState.syncTime.format(formatter)}")
                        }
                    }
                    if (key == "DELAYED" && uiState.syncOption == "DELAYED") {
                        TextButton(onClick = onDelayTimeClick, contentPadding = PaddingValues(start = 0.dp)) {
                             Text("지연 시간 설정: ${uiState.syncDelayHours}시간 ${uiState.syncDelayMinutes}분 뒤")
                        }
                    }
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

    Column {
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
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("로그아웃") }
        } else {
            Button(
                onClick = if (selectedProvider == "google_photos") onGoogleLoginClick else onNaverLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedProvider == "google_photos") "Google 계정으로 로그인" else "Naver 계정으로 로그인")
            }
        }
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
        Text(title, fontSize = 16.sp)
        content()
    }
}
