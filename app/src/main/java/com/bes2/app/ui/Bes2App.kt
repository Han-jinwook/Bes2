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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.bes2.app.R
import com.bes2.app.ui.component.TypewriterText
import com.bes2.app.ui.home.HomeUiState
import com.bes2.app.ui.home.HomeViewModel
import com.bes2.app.ui.home.ReportStats
import com.bes2.app.ui.review.ReviewScreen
import com.bes2.app.ui.screenshot.ScreenshotScreen
import com.bes2.app.ui.search.SearchScreen
import com.bes2.app.ui.settings.SettingsScreen
import java.time.LocalDate

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
                onNavigateToSearch = { navController.navigate("search") }, // New Navigation
                onNavigateToReview = { date ->
                    if (date != null) {
                        navController.navigate("review?date=$date")
                    } else {
                        navController.navigate("review")
                    }
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
            route = "review?date={date}",
            arguments = listOf(navArgument("date") { 
                type = NavType.StringType
                nullable = true 
            })
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
        // New Route
        composable("search") {
            SearchScreen(
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
    onNavigateToSearch: () -> Unit, // New callback
    onNavigateToReview: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showGuideDialog by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
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
                .padding(top = 28.dp, end = 4.dp)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // --- APP LOGO & SLOGAN ---
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
                Column(verticalArrangement = Arrangement.Center) {
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
            
            // --- NEW 2x2 GRID UI ---
            // Row 1: Report (Top Left) & Search (Top Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Report Card (Smaller Version)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp) // Fixed height
                        .clickable { showReportDialog = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "오늘 정리",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Column {
                            Text(
                                text = "${uiState.dailyDeleted}장",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "삭제 완료",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Report",
                            modifier = Modifier.align(Alignment.End).size(16.dp)
                        )
                    }
                }

                // 2. AI Search Card (New)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer, // Distinct color
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clickable { onNavigateToSearch() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI 검색",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Typewriter Effect
                        val searchExamples = listOf("\"웃는 아이\"", "\"맛있는 파스타\"", "\"푸른 바다\"", "\"생일 파티\"")
                        TypewriterText(
                            texts = searchExamples,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                        
                         Text(
                            text = "자연어로 찾기",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Search",
                            modifier = Modifier.align(Alignment.End).size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Gallery Diet (Bottom Left) & Screenshots (Bottom Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 3. Gallery Diet
                val isReadyToClean = uiState.readyToCleanCount > 0
                val dietCardColor = if (isReadyToClean) Color(0xFFFFCC80) else MaterialTheme.colorScheme.surfaceVariant // Orange pastel if ready
                val dietContentColor = if (isReadyToClean) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = dietCardColor, contentColor = dietContentColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .scale(if (isReadyToClean) pulseScale else 1f)
                        .clickable(enabled = isReadyToClean) {
                             if (isReadyToClean) onNavigateToReview(null)
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                     Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("갤러리 정리", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        if (isReadyToClean) {
                            Text("${uiState.readyToCleanCount}장", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("대기 중", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("준비 중...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // 4. Screenshot Cleaner
                val hasScreenshots = uiState.screenshotCount > 0
                val screenshotCardColor = if (hasScreenshots) Color(0xFFEF9A9A) else MaterialTheme.colorScheme.surfaceVariant // Red pastel
                val screenshotContentColor = if (hasScreenshots) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant

                Card(
                    colors = CardDefaults.cardColors(containerColor = screenshotCardColor, contentColor = screenshotContentColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .scale(if (hasScreenshots) pulseScale else 1f)
                        .clickable { onNavigateToScreenshotClean() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("문서/캡처", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("${uiState.screenshotCount}장", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(if (hasScreenshots) "정리하기" else "없음", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // --- MEMORY EVENT CARD (Full Width below Grid) ---
            Spacer(modifier = Modifier.height(12.dp))
            val memoryEvent = uiState.memoryEvent
            if (memoryEvent != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReview(memoryEvent.date) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎉  ${memoryEvent.date}의 추억 (${memoryEvent.count}장) 확인하기", fontWeight = FontWeight.Bold)
                    }
                }
                 Spacer(modifier = Modifier.height(12.dp))
            }
            

            Spacer(modifier = Modifier.height(24.dp))

            // Main Action Button (Bottom)
            val hasPending = uiState.hasPendingReview
            OutlinedButton(
                onClick = {
                    if (hasPending) {
                        onNavigateToReview(null)
                    } else {
                        onStartAnalysisAndExit()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                border = BorderStroke(1.dp, Color.Blue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue)
            ) {
                Text(
                    text = if (hasPending) "분석된 사진묶음 정리하기" else "정리 끝! 이제 사진 찍으러 가자",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BES2 TIPS SECTION ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f).height(IntrinsicSize.Max),
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
        }
        
        if (showGuideDialog) GuideDialog(onDismiss = { showGuideDialog = false })
        if (showTipsDialog) TipsSelectionDialog(onDismiss = { showTipsDialog = false }, onCloudTip = { showTipsDialog = false; launchAIWithPrompt("...") }, onPcTip = { showTipsDialog = false; launchAIWithPrompt("...") })
        if (showReportDialog) ReportDialog(uiState = uiState, onDismiss = { showReportDialog = false })
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
                    modifier = Modifier.size(160.dp) // Increased size
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
