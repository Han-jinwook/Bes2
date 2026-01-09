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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.bes2.app.BuildConfig
import com.bes2.app.R
import com.bes2.app.ui.home.HomeUiState
import com.bes2.app.ui.home.HomeViewModel
import com.bes2.app.ui.review.ReviewScreen
import com.bes2.app.ui.screenshot.ScreenshotScreen
import com.bes2.app.ui.settings.SettingsScreen
import java.time.LocalDate

@Composable
fun Bes2App(
    onStartAnalysisAndExit: () -> Unit,
    pendingNavigationEvent: String? = null
) {
    val context = LocalContext.current

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsToRequest = remember {
        mutableListOf(
            storagePermission
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasStoragePermission = ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasStoragePermission) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (hasStoragePermission) {
            val navController = rememberNavController()
            
            LaunchedEffect(pendingNavigationEvent) {
                if (pendingNavigationEvent == "SCREENSHOT_CLEAN") {
                    navController.navigate("screenshot_clean")
                }
            }
            
            AppNavigation(navController = navController, onStartAnalysisAndExit = onStartAnalysisAndExit)
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "앱을 사용하려면 저장 공간 접근 권한을 허용해야 합니다.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(permissionsToRequest) }) {
                        Text("권한 허용하기")
                    }
                }
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
            val homeViewModel: HomeViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                homeViewModel.triggerBackgroundScan()
            }
            
            HomeScreen(
                viewModel = homeViewModel,
                onStartAnalysisAndExit = onStartAnalysisAndExit,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToScreenshotClean = { navController.navigate("screenshot_clean") },
                onNavigateToReview = { date, sourceType ->
                    val route = if (date != null) {
                        "review?date=$date&source_type=$sourceType"
                    } else {
                        "review?source_type=$sourceType"
                    }
                    navController.navigate(route)
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "review?date={date}&source_type={source_type}",
            arguments = listOf(
                navArgument("date") { 
                    type = NavType.StringType
                    nullable = true 
                },
                navArgument("source_type") { 
                    type = NavType.StringType
                    nullable = true 
                    defaultValue = "DIET"
                }
            )
        ) {
            ReviewScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
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
    onNavigateToReview: (String?, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showGuideDialog by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showIntroShareDialog by remember { mutableStateOf(false) } // [ADDED]
    
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.triggerBackgroundScan()
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
        try {
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f, 
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // [MODIFIED] Logo is now clickable to show Intro/Share Dialog
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showIntroShareDialog = true }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.clickable { showIntroShareDialog = true },
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

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "오늘의 정리 리포트",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showReportDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "📊 상세 리포트",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Show Report",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${uiState.dailyTotal}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text(text = "촬영", style = MaterialTheme.typography.labelSmall)
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                                    .align(Alignment.CenterVertically)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.dailyKept}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF7043)
                                )
                                Text(text = "저장", style = MaterialTheme.typography.labelSmall)
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                                    .align(Alignment.CenterVertically)
                            )

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

                Spacer(modifier = Modifier.height(32.dp))

                val memoryEvent = uiState.memoryEvent
                val isMemoryActive = memoryEvent != null && uiState.isMemoryPrepared
                val eventCardColor = if (isMemoryActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                val eventContentColor = if (isMemoryActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                val eventFontWeight = if (isMemoryActive) FontWeight.Bold else FontWeight.Normal

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = eventCardColor,
                        contentColor = eventContentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isMemoryActive) pulseScale else 1f)
                        .clickable(enabled = isMemoryActive) {
                            if (memoryEvent != null) {
                                onNavigateToReview(memoryEvent.date, "MEMORY")
                            }
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isMemoryActive && memoryEvent != null) {
                            AsyncImage(
                                model = memoryEvent.representativeUri,
                                contentDescription = "Memory Thumbnail",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "추억 소환 🎉",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = eventFontWeight,
                                    color = Color.White
                                )
                                Text(
                                    text = "${memoryEvent.date}의 추억 (${memoryEvent.count}장) 정리하기",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = eventFontWeight,
                                    color = Color.White
                                )
                            }
                        } else {
                            val memoryText = when {
                                memoryEvent != null -> "추억을 분석하고 있습니다..."
                                uiState.isAnalysisInProgress -> "숨어있는 추억을 찾는 중..."
                                else -> "발견된 추억이 없습니다"
                            }
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Memory",
                                modifier = Modifier.size(24.dp),
                                tint = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = memoryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = eventFontWeight,
                                color = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val isDietActive = !uiState.isAnalysisInProgress && uiState.readyToCleanCount > 0
                val dietCardColor = if (isDietActive) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                val dietContentColor = if (isDietActive) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                val dietFontWeight = if (isDietActive) FontWeight.Bold else FontWeight.Normal

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = dietCardColor,
                        contentColor = dietContentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isDietActive) pulseScale else 1f)
                        .clickable(enabled = isDietActive) {
                            if (isDietActive) {
                                onNavigateToReview(null, "DIET")
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
                                imageVector = if (isDietActive) Icons.Default.CleaningServices else Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery Diet",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "갤러리 다이어트",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = dietFontWeight
                                )
                                if (uiState.isAnalysisInProgress) {
                                    val progressText = if (uiState.analysisProgressTotal > 0) {
                                        "AI 심층 분석 중... (${uiState.analysisProgressCurrent} / ${uiState.analysisProgressTotal})"
                                    } else {
                                        "전체 갤러리 분석 중... (준비 중)"
                                    }
                                    Text(
                                        text = progressText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = dietFontWeight
                                    )
                                } else if (isDietActive) {
                                    Text(
                                        text = "전체 ${uiState.galleryTotalCount}장 중 ${uiState.readyToCleanCount}장 준비됨",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = dietFontWeight
                                    )
                                } else {
                                    Text(
                                        text = "현재 갤러리 ${uiState.galleryTotalCount}장",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        if (isDietActive) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val hasScreenshots = uiState.screenshotCount > 0
                val isDiscoveryActive = !uiState.isDiscoveryInProgress
                val isTrashActive = isDiscoveryActive && hasScreenshots
                val screenshotCardColor = if (isTrashActive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
                val screenshotContentColor = if (isTrashActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                val screenshotFontWeight = if (isTrashActive) FontWeight.Bold else FontWeight.Normal

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = screenshotCardColor,
                        contentColor = screenshotContentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isTrashActive) pulseScale else 1f)
                        .clickable(enabled = isTrashActive) { onNavigateToScreenshotClean() },
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
                                    text = "스크린샷/불필요 사진 비우기",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = screenshotFontWeight
                                )
                                if (uiState.isDiscoveryInProgress) {
                                    Text(
                                        text = "전체 갤러리 분류 중... (잠시만요)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    Text(
                                        text = if (hasScreenshots) "정리할 사진 ${uiState.screenshotCount}장 발견 (지금 바로 비우기)" else "정리할 사진 0장",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        if (isTrashActive) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val hasPending = uiState.hasPendingReview
                OutlinedButton(
                    onClick = {
                        if (hasPending) {
                            onNavigateToReview(null, "INSTANT")
                        } else {
                            onStartAnalysisAndExit()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .scale(if (hasPending) pulseScale else 1f),
                    border = BorderStroke(1.dp, Color.Blue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue)
                ) {
                    Text(
                        text = if (hasPending) "분석된 사진묶음 정리하기"
                               else "정리 끝! 이제 사진 찍으러 가자",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f, fill = true))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(IntrinsicSize.Max),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "꿀\nTip",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                    }

                    Column(
                        modifier = Modifier.width(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedButton(
                            onClick = { showGuideDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bes2 100% 활용법", fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showTipsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.TipsAndUpdates, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("꿀 클라우드/PC 정리 꿀팁", fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Version Name
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        // Settings button is now drawn last to be on top
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToSettings() }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "동기화",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        if (showGuideDialog) {
            GuideDialog(onDismiss = { showGuideDialog = false })
        }
        
        if (showTipsDialog) {
            TipsSelectionDialog(
                onDismiss = { showTipsDialog = false },
                onCloudTip = {
                    showTipsDialog = false
                    val promptText = "안녕! 나는 'Bes2(베스트투)'라는 앱으로 폰 사진을 정리하고 있어.\n" +
                            "나는 구글 포토 말고 **'네이버 마이박스(MyBox)'**나 다른 클라우드를 주력으로 사용해.\n\n" +
                            "내 폰의 모든 잡동사니 사진이 클라우드에 자동으로 다 올라가서 용량을 차지하는 게 싫어.\n" +
                            "Bes2로 '베스트 컷'만 남긴 뒤에 깔끔하게 백업하고 싶은데, **'평소엔 자동 동기화를 꺼두고, 정리가 끝났을 때만 수동으로 백업하는 노하우'**를 단계별로 아주 쉽게 알려줘.\n\n" +
                            "(팁: 갤러리 정리 후 '수동 올리기'나 '동기화 잠시 켜기' 같은 방법 위주로 설명해 줘)"
                    launchAIWithPrompt(promptText)
                },
                onPcTip = {
                    showTipsDialog = false
                    val promptText = "나는 지금 휴대폰 사진을 PC로 옮겨서 정리하려고 해. 수천 장의 사진을 효율적으로 분류하고, 중복되거나 흔들린 사진을 빠르게 골라내는 기준과 팁을 알려줘. 그리고 날짜별/주제별 폴더 구조 추천해줘."
                    launchAIWithPrompt(promptText)
                }
            )
        }

        if (showReportDialog) {
            ReportDialog(
                uiState = uiState,
                onDismiss = { showReportDialog = false }
            )
        }

        // [ADDED] App Intro & Share Dialog
        if (showIntroShareDialog) {
            AppIntroShareDialog(onDismiss = { showIntroShareDialog = false })
        }
    }
}

@Composable
fun AppIntroShareDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val appLink = "https://play.google.com/store/apps/details?id=com.bes2.app"
    val introTitle = "Bes2 (베스트투)"
    val introSubtitle = "AI 기반 갤러리 정리 비서"
    val fullShareText = "📸 복잡한 갤러리, AI 비서 Bes2가 정리해드려요! 베스트 사진만 남기고 용량 고민 끝.\n지금 다운로드: $appLink"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp), // Push to bottom like a sheet
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bes2 소개",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Intro Box (Similar to ReperMeta)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = introTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = introSubtitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        IntroFeatureItem(Icons.Default.AutoFixHigh, "스마트 분류", "인물, 풍경, 음식 사진만 쏙쏙 골라내요.")
                        IntroFeatureItem(Icons.Default.Collections, "갤러리 다이어트", "비슷한 사진 중 베스트 컷만 남겨드려요.")
                        IntroFeatureItem(Icons.Default.DeleteSweep, "비우기 한판", "스크린샷과 잡동사니를 한 번에 정리해요.")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "친구에게 공유하기", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShareButton(
                        icon = Icons.Default.Link,
                        label = "링크 복사",
                        backgroundColor = Color(0xFF03A9F4),
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Bes2 App Link", appLink)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "링크가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    ShareButton(
                        icon = Icons.Default.Share,
                        label = "앱 추천하기",
                        backgroundColor = Color(0xFF4CAF50),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, fullShareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Bes2 공유하기"))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun IntroFeatureItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun RowScope.ShareButton(icon: ImageVector, label: String, backgroundColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
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

@Composable
fun TipsSelectionDialog(
    onDismiss: () -> Unit,
    onCloudTip: () -> Unit,
    onPcTip: () -> Unit
) {
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
                    text = "꿀팁 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onCloudTip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("다른 클라우드 이용법 (AI)")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onPcTip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Computer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PC 큰 화면으로 정리하기 (AI)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("닫기")
                }
            }
        }
    }
}

@Composable
fun ReportDialog(
    uiState: HomeUiState,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Monthly, 1: Yearly
    val currentStats = if (selectedTab == 0) uiState.monthlyReport else uiState.yearlyReport
    val currentDate = LocalDate.now()
    val periodText = if (selectedTab == 0) "${currentDate.year}년 ${currentDate.monthValue}월" else "${currentDate.year}년"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "나의 정리 성과",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        text = "월간",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    TabButton(
                        text = "연간",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Period
                Text(
                    text = periodText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Stats Chart (Simple Pie Chart)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    if (currentStats.total > 0) {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            val total = currentStats.total.toFloat()
                            val keptAngle = (currentStats.kept / total) * 360f
                            val deletedAngle = (currentStats.deleted / total) * 360f
                            
                            drawArc(
                                color = Color(0xFFFF7043), // Kept Color
                                startAngle = -90f,
                                sweepAngle = keptAngle,
                                useCenter = false,
                                style = Stroke(width = 36f)
                            )
                            drawArc(
                                color = Color.Gray.copy(alpha = 0.3f), // Deleted Color (Lighter)
                                startAngle = -90f + keptAngle,
                                sweepAngle = deletedAngle,
                                useCenter = false,
                                style = Stroke(width = 36f)
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.size(140.dp)) {
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 36f)
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${currentStats.total}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                     DetailItem(label = "남긴 사진", value = "${currentStats.kept}장", color = Color(0xFFFF7043))
                     DetailItem(label = "정리한 사진", value = "${currentStats.deleted}장", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Efficiency Message Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI 비서의 기여도",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${currentStats.efficiency}% 더 효율적!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "AI가 ${currentStats.deleted}장의 B컷 정리를 도왔어요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

@Composable
fun RowScope.TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun DetailItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}
