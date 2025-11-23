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
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    var showGuideDialog by remember { mutableStateOf(false) }
    
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

    fun launchAIWithPrompt(promptText: String) {
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

        if (tryLaunchApp("com.google.android.apps.bard")) return
        if (tryLaunchApp("com.google.android.googlequicksearchbox")) return
        if (tryLaunchApp("com.openai.chatgpt")) return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bes2 AI Prompt", promptText)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(context, "AI에게 질문할 내용이 복사되었습니다. 붙여넣으세요!", Toast.LENGTH_LONG).show()

        val uri = Uri.parse("https://gemini.google.com/")
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(browserIntent)
    }

    // Define shared Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f, // Slightly smaller scale for cards to avoid layout jump
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Settings Button (Top Right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .clickable { onNavigateToSettings() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )
            Text(
                text = "동기화", 
                color = Color.Gray, 
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // --- APP LOGO & SLOGAN (Left Aligned) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo), 
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp) 
                        .clip(RoundedCornerShape(16.dp))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "AI 사진비서",
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "복잡한 갤러리,\nBest 2장으로 완성",
                        style = MaterialTheme.typography.titleMedium, 
                        color = Color(0xFFFF7043), 
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- DAILY REPORT CARD (Compact) ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Title
                    Text(
                        text = "오늘의\n정리 리포트",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    )
                    
                    // Right Stats
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${uiState.dailyTotal}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(text = "촬영", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.dailyKept}", 
                                style = MaterialTheme.typography.headlineMedium, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFFFF7043)
                            )
                            Text(text = "저장", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.dailyDeleted}", 
                                style = MaterialTheme.typography.headlineMedium, 
                                fontWeight = FontWeight.Bold 
                            )
                            Text(text = "삭제", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) 

            // --- GALLERY DIET CARD (Large) ---
            val isReadyToClean = uiState.readyToCleanCount > 0
            val dietCardColor = if (isReadyToClean) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val dietContentColor = if (isReadyToClean) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = dietCardColor,
                    contentColor = dietContentColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (isReadyToClean) pulseScale else 1f) // Pulse if ready
                    .clickable(enabled = isReadyToClean) {
                        if (isReadyToClean) {
                            onNavigateToReview()
                        }
                    },
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
                            imageVector = if (isReadyToClean) Icons.Default.CleaningServices else Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery Diet",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "갤러리 다이어트",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isReadyToClean) {
                                Text(
                                    text = "${uiState.readyToCleanCount}장 준비됨 (시작하기)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "현재 갤러리: ${uiState.galleryTotalCount}장 (준비 중...)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    if (isReadyToClean) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SCREENSHOT CLEANER CARD ---
            val hasScreenshots = uiState.screenshotCount > 0
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (hasScreenshots) pulseScale else 1f) // Pulse if screenshots exist
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
                                text = if (hasScreenshots) "${uiState.screenshotCount}장 발견 (지금 바로 비우기)" else "정리할 스크린샷이 없습니다",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Main Action Button (Exit / Background Mode) with Animation
            val hasPending = uiState.hasPendingReview
            
            Button(
                onClick = {
                    if (hasPending) {
                        onNavigateToReview()
                    } else {
                        onStartAnalysisAndExit()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(if (hasPending) pulseScale else 1f), // Use shared pulseScale
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(
                    text = if (hasPending) "분석된 사진묶음 정리하기" 
                           else "정리 끝! 이제 사진 찍으러 가기", 
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BES2 TIPS SECTION ---
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bes2 꿀Tip",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 1. Guide
                OutlinedButton(
                    onClick = { showGuideDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bes2 100% 활용법", fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                // 2. Cloud Tip
                OutlinedButton(
                    onClick = {
                        val promptText = "안녕! 나는 'Bes2(베스트투)'라는 앱으로 폰 사진을 정리하고 있어.\n" +
                                "나는 구글 포토 말고 **'네이버 마이박스(MyBox)'**나 다른 클라우드를 주력으로 사용해.\n\n" +
                                "내 폰의 모든 잡동사니 사진이 클라우드에 자동으로 다 올라가서 용량을 차지하는 게 싫어.\n" +
                                "Bes2로 '베스트 컷'만 남긴 뒤에 깔끔하게 백업하고 싶은데, **'평소엔 자동 동기화를 꺼두고, 정리가 끝났을 때만 수동으로 백업하는 노하우'**를 단계별로 아주 쉽게 알려줘.\n\n" +
                                "(팁: 갤러리 정리 후 '수동 올리기'나 '동기화 잠시 켜기' 같은 방법 위주로 설명해 줘)"
                        launchAIWithPrompt(promptText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("다른 클라우드 이용법 (AI)", fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. PC Tip
                OutlinedButton(
                    onClick = {
                        val promptText = "나는 지금 휴대폰 사진을 PC로 옮겨서 정리하려고 해. 수천 장의 사진을 효율적으로 분류하고, 중복되거나 흔들린 사진을 빠르게 골라내는 기준과 팁을 알려줘. 그리고 날짜별/주제별 폴더 구조 추천해줘."
                        launchAIWithPrompt(promptText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PC 큰 화면으로 정리하기 (AI)", fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        if (showGuideDialog) {
            GuideDialog(onDismiss = { showGuideDialog = false })
        }
    }
}

@Composable
fun GuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bes2 100% 활용하기",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "1. 마음껏 찍으세요! 나머지는 Bes2가 알아서 합니다.\n\n" +
                           "2. 앱을 켜두면 과거 사진을 야금야금 정리해드립니다.\n\n" +
                           "3. 베스트 사진만 클라우드에 자동 백업됩니다.",
                    textAlign = TextAlign.Start,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) {
                    Text("알겠습니다")
                }
            }
        }
    }
}
