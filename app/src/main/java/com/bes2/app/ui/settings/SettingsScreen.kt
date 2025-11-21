package com.bes2.app.ui.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
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
            // Removed title "클라우드 자동 동기화"
            Spacer(modifier = Modifier.height(8.dp))

            // Google Photos Only UI
            CloudProviderSection(
                isLoggedIn = uiState.isLoggedIn,
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
                onLogoutClick = viewModel::onLogoutClicked
            )

            // Tip for other cloud users
            Spacer(modifier = Modifier.height(8.dp))
            OtherCloudTipAccordion()

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            UnifiedSyncOptions(
                uiState = uiState,
                onOptionSelected = viewModel::onSyncOptionChanged,
                onDailyTimeClick = { showDailyTimePicker = true },
                onDelayTimeClick = { showDelayTimePicker = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
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
                        // Changed TextButton to Text with clickable modifier to remove extra padding
                        Text(
                            text = "매일 ${uiState.syncTime.format(formatter)}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 0.dp) // Remove top padding
                                .clickable { onDailyTimeClick() }
                        )
                    }
                    if (key == "DELAYED" && uiState.syncOption == "DELAYED") {
                         // Changed TextButton to Text with clickable modifier to remove extra padding
                         Text(
                             text = "지연 시간 설정: ${uiState.syncDelayHours}시간 ${uiState.syncDelayMinutes}분 뒤",
                             color = MaterialTheme.colorScheme.primary,
                             modifier = Modifier
                                 .padding(top = 0.dp) // Remove top padding
                                 .clickable { onDelayTimeClick() }
                         )
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudProviderSection(
    isLoggedIn: Boolean,
    onGoogleLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Removed subtitle "연결된 클라우드"
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Google 포토 자동 동기화", // Updated Text
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold // Changed to Bold for emphasis
                )
                
                if (isLoggedIn) {
                    OutlinedButton(onClick = onLogoutClick) {
                        Text("로그아웃")
                    }
                } else {
                    Button(onClick = onGoogleLoginClick) {
                        Text("로그인")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Google Photos Guide
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "평소 '구글 포토' 앱의 [백업] 기능은 OFF로 해두세요.\nBes2가 정리한 베스트 사진만 백업됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OtherCloudTipAccordion() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "다른 클라우드(네이버 MyBox 등) 이용 팁",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "평소 클라우드 자동 백업 기능은 꺼두세요.\n" +
                               "Bes2 앱이 사진 정리를 완료하면, 그때 원하는 클라우드 앱(네이버 MyBox, One Drive 등)을 열어 수동으로 동기화하시면 가장 깔끔하게 정리된 사진만 백업할 수 있습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
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
