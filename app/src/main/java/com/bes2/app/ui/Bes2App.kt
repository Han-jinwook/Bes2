package com.bes2.app.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bes2.app.R
import com.bes2.app.ui.home.HomeViewModel
import com.bes2.app.ui.review.ReviewScreen
import com.bes2.app.ui.screenshot.ScreenshotScreen
import com.bes2.app.ui.settings.SettingsScreen

/**
 * Main entry point for the app's UI.
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
            HomeScreen(
                viewModel = hiltViewModel(),
                onStartAnalysisAndExit = onStartAnalysisAndExit,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToScreenshotClean = { navController.navigate("screenshot_clean") },
                onNavigateToReview = { navController.navigate("review") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("review") {
            ReviewScreen(viewModel = hiltViewModel())
        }
        composable("screenshot_clean") {
            ScreenshotScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    viewModel: HomeViewModel,
    onStartAnalysisAndExit: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToScreenshotClean: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isStandbyMode by remember { mutableStateOf(false) }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshGalleryCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Settings Button (Top Right)
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(28.dp),
                tint = Color.Gray
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- APP LOGO & SLOGAN ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo), 
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(148.dp) 
                        .clip(RoundedCornerShape(16.dp))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "AI 포토 큐레이터",
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "복잡한 갤러리,\nBest 2장으로 완성",
                        style = MaterialTheme.typography.titleMedium, 
                        color = Color(0xFFFF7043), 
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // --- GALLERY STATUS BADGE ---
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "현재 갤러리: ${uiState.galleryTotalCount}장",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " (조금씩 비워볼까요?)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // --- DAILY REPORT CARD ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "오늘의 정리 리포트",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${uiState.dailyTotal}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            Text(text = "촬영", style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.dailyKept}", 
                                style = MaterialTheme.typography.headlineLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFFFF7043)
                            )
                            Text(text = "저장", style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.dailyDeleted}", 
                                style = MaterialTheme.typography.headlineLarge, 
                                fontWeight = FontWeight.Bold 
                            )
                            Text(text = "삭제", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SCREENSHOT CLEANER CARD ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToScreenshotClean() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clean Screenshots",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "스크린샷 청소하기",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.screenshotCount > 0) "${uiState.screenshotCount}장 발견 (지금 바로 비우기)" else "정리할 스크린샷이 없습니다",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Main Action Button (Dynamic)
            val hasPending = uiState.hasPendingReview
            Button(
                onClick = {
                    if (hasPending) {
                        onNavigateToReview()
                    } else {
                        isStandbyMode = true
                        onStartAnalysisAndExit()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = if (isStandbyMode && !hasPending) ButtonDefaults.buttonColors(containerColor = Color.Gray) else ButtonDefaults.buttonColors()
            ) {
                // Updated Text for clarity
                Text(
                    text = if (hasPending) "리뷰 이어서 하기 (분석 완료)" 
                           else if (isStandbyMode) "홈 화면으로 나가기 (백그라운드 감지 중)" 
                           else "홈 화면으로 나가기 (백그라운드 감지 중)", 
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PC Link Button (AI Toss)
            TextButton(
                onClick = {
                    val promptText = "나는 지금 휴대폰 사진을 PC로 옮겨서 정리하려고 해. 수천 장의 사진을 효율적으로 분류하고, 중복되거나 흔들린 사진을 빠르게 골라내는 기준과 팁을 알려줘. 그리고 날짜별/주제별 폴더 구조 추천해줘."

                    fun tryLaunchApp(packageName: String): Boolean {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            setPackage(packageName)
                            putExtra(Intent.EXTRA_TEXT, promptText)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                            return true
                        }
                        return false
                    }

                    if (tryLaunchApp("com.google.android.apps.bard")) return@TextButton
                    if (tryLaunchApp("com.google.android.googlequicksearchbox")) return@TextButton
                    if (tryLaunchApp("com.openai.chatgpt")) return@TextButton

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Bes2 AI Prompt", promptText)
                    clipboard.setPrimaryClip(clip)
                    
                    Toast.makeText(context, "AI에게 질문할 내용이 복사되었습니다. 붙여넣으세요!", Toast.LENGTH_LONG).show()

                    val uri = Uri.parse("https://gemini.google.com/")
                    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(browserIntent)
                }
            ) {
                Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PC 큰 화면으로 정리하기 (AI 도움받기)")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
