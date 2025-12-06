package com.bes2.app.ui.review

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.bes2.app.R
import com.bes2.background.worker.PhotoAnalysisWorker
import com.bes2.data.model.ReviewItemEntity 
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // [ADDED] Handle System Back Button
    BackHandler {
        viewModel.onExitClicked()
    }

    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    LaunchedEffect(Unit) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, "ca-app-pub-6474204369625572/5414444948", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) { interstitialAd = null }
            override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
        })
    }

    fun showInterstitialAndNavigate() {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { onNavigateBack() }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) { onNavigateBack() }
            }
            if (context is Activity) ad.show(context) else onNavigateBack()
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(key1 = viewModel.navigationEvent) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToHome -> {
                    Toast.makeText(context, "${event.clusterCount}개 묶음 중 베스트 ${event.savedCount}장을 정리했습니다.", Toast.LENGTH_LONG).show()
                    if (event.showAd) { delay(4000); showInterstitialAndNavigate() } else { delay(2000); onNavigateBack() }
                }
                is NavigationEvent.PopBackStack -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    var zoomedImageState by remember { mutableStateOf<ZoomedImageState?>(null) }
    val prefs = remember { context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE) }
    var showCoachMark by remember { mutableStateOf(prefs.getInt("coach_mark_show_count", 0) < 2) }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeletionRequestHandled(result.resultCode == AppCompatActivity.RESULT_OK)
    }

    when (val state = uiState) {
        is ReviewUiState.Loading -> LoadingState()
        is ReviewUiState.NoClustersToReview -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        is ReviewUiState.Ready -> {
            state.pendingDeleteRequest?.let { urisToDelete ->
                LaunchedEffect(urisToDelete) {
                    val intentSender = MediaStore.createDeleteRequest(context.contentResolver, urisToDelete).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            }

            val pagerState = rememberPagerState(
                initialPage = state.currentClusterIndex - 1,
                pageCount = { state.totalClusterCount }
            )

            LaunchedEffect(pagerState.currentPage) {
                if (state.currentClusterIndex - 1 != pagerState.currentPage) {
                     if (pagerState.currentPage > state.currentClusterIndex - 1) viewModel.nextCluster()
                     else viewModel.prevCluster()
                }
            }
            
            LaunchedEffect(state.currentClusterIndex) {
                if (pagerState.currentPage != state.currentClusterIndex - 1) {
                    pagerState.animateScrollToPage(state.currentClusterIndex - 1)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // [MODIFIED] Use viewModel.onExitClicked()
                                IconButton(onClick = { viewModel.onExitClicked() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Image(
                                    painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = "App Logo",
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI 사진비서", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Box(
                                    modifier = Modifier
                                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1} / ${state.totalClusterCount}", 
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier.height(64.dp)
                    )
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        val hasTrash = state.otherImages.isNotEmpty() || state.rejectedImages.isNotEmpty()
                        val buttonText = if (hasTrash) "나머지/실패 사진 삭제하고 완료" else "선택한 사진 보관하고 완료"
                        val buttonColor = if (hasTrash) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        
                        Button(
                            onClick = { viewModel.deleteOtherImages() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(60.dp), contentAlignment = Alignment.Center) {
                            AndroidView(factory = { ctx ->
                                AdView(ctx).apply {
                                    setAdSize(AdSize.BANNER)
                                    adUnitId = "ca-app-pub-6474204369625572/7317843117"
                                    loadAd(AdRequest.Builder().build())
                                }
                            })
                        }
                    }
                }
            ) { padding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    userScrollEnabled = true
                ) { page ->
                    if (page == state.currentClusterIndex - 1) {
                        ReviewReadyState(
                            state = state,
                            onImageClick = { image -> viewModel.selectImage(image) },
                            onImageLongPress = { sectionIndex, index ->
                                val best = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage)
                                zoomedImageState = ZoomedImageState(best, state.otherImages, state.rejectedImages, sectionIndex, index)
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
// ... [Remaining UI components same as before]
            zoomedImageState?.let {
                ZoomedImageDialogV2(
                    state = it,
                    onDismiss = { zoomedImageState = null },
                    onRestoreClick = { image -> viewModel.restoreImage(image); zoomedImageState = null }
                )
            }

            if (showCoachMark && (state.selectedBestImage != null || state.otherImages.isNotEmpty())) {
                CoachMark(
                    text = "사진을 길게 눌러 크게 확인하고,\n실패한 사진은 탭 하여 되살리세요!\n(좌우로 밀어서 다른 사진도 확인해보세요)",
                    onDismiss = {
                        val currentCount = prefs.getInt("coach_mark_show_count", 0)
                        prefs.edit().putInt("coach_mark_show_count", currentCount + 1).apply()
                        showCoachMark = false
                    }
                )
            }
        }
    }
}

data class ZoomedImageState(
    val bestList: List<ReviewItemEntity>,
    val otherList: List<ReviewItemEntity>,
    val rejectedList: List<ReviewItemEntity>,
    val initialSection: Int,
    val initialIndex: Int
)

@Composable
fun LoadingState() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "검토할 사진을 찾고 있습니다...")
    }
}

@Composable
fun NoClustersState() {}

@Composable
fun ReviewReadyState(
    state: ReviewUiState.Ready,
    onImageClick: (ReviewItemEntity) -> Unit,
    onImageLongPress: (Int, Int) -> Unit 
) {
    val bestImages = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("베스트 2장", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Row(modifier = Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            bestImages.getOrNull(0)?.let { image ->
                ImageWithInfo(image = image, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)).pointerInput(image) { detectTapGestures(onTap = { onImageClick(image) }, onLongPress = { onImageLongPress(0, 0) }) })
            }
            bestImages.getOrNull(1)?.let { image ->
                ImageWithInfo(image = image, modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp)).pointerInput(image) { detectTapGestures(onTap = { onImageClick(image) }, onLongPress = { onImageLongPress(0, 1) }) })
            } ?: Box(modifier = Modifier.weight(1f).fillMaxSize())
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
            Text("나머지 사진", style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
            Text(" (${state.otherImages.size}장)", style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Bold)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(state.otherImages) { index, image ->
                ImageWithInfo(image = image, modifier = Modifier.height(80.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).pointerInput(image) { detectTapGestures(onTap = { onImageClick(image) }, onLongPress = { onImageLongPress(1, index) }) })
            }
        }
        if (state.rejectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Text("실패한 사진", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                Text(" (${state.rejectedImages.size}장)", style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Bold)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.rejectedImages) { index, image ->
                    ImageWithInfo(image = image, modifier = Modifier.height(100.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).pointerInput(image) { detectTapGestures(onTap = { onImageClick(image) }, onLongPress = { onImageLongPress(2, index) }) })
                }
            }
        }
    }
}

@Composable
fun ImageWithInfo(image: ReviewItemEntity, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        AsyncImage(model = image.uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        val infoText = if (image.status == "STATUS_REJECTED") {
            if (image.areEyesClosed == true) "눈 감김" else if ((image.blurScore ?: 100f) < 30.0f) "흐릿함" else "품질 저하" // [FIX] Hardcode threshold or import worker constant properly
        } else {
            val score = ((image.nimaScore ?: 0.0) * 10 + (if ((image.smilingProbability ?: 0f) < 0.1f) -10f else (image.smilingProbability ?: 0f) * 30f)).toInt().coerceAtLeast(1)
            "${score}점"
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp)).padding(4.dp, 2.dp)) {
            Text(infoText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ZoomedImageDialogV2(state: ZoomedImageState, onDismiss: () -> Unit, onRestoreClick: ((ReviewItemEntity) -> Unit)? = null) {
    Dialog(onDismissRequest = onDismiss) {
        var currentSection by remember { mutableStateOf(state.initialSection) }
        var currentIndex by remember { mutableStateOf(state.initialIndex) }

        fun getNextSection(start: Int): Int? {
            for (i in start + 1..2) {
                val list = when(i) { 0 -> state.bestList; 1 -> state.otherList; 2 -> state.rejectedList; else -> emptyList() }
                if (list.isNotEmpty()) return i
            }
            return null
        }

        fun getPrevSection(start: Int): Int? {
            for (i in start - 1 downTo 0) {
                val list = when(i) { 0 -> state.bestList; 1 -> state.otherList; 2 -> state.rejectedList; else -> emptyList() }
                if (list.isNotEmpty()) return i
            }
            return null
        }

        val currentList = when (currentSection) {
            0 -> state.bestList
            1 -> state.otherList
            2 -> state.rejectedList
            else -> emptyList()
        }
        
        if (currentList.isEmpty()) {
            LaunchedEffect(Unit) { onDismiss() }
            return@Dialog
        }
        
        val currentImage = currentList.getOrNull(currentIndex) ?: currentList[0]

        val (sectionColor, sectionTitle) = when (currentSection) {
            0 -> Color(0xFF4CAF50) to "베스트"
            1 -> Color(0xFFFFC107) to "나머지"
            2 -> Color(0xFFF44336) to "실패"
            else -> Color.White to ""
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                offset += offsetChange
            }

            LaunchedEffect(currentImage) {
                scale = 1f
                offset = Offset.Zero
            }

            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = currentImage.uri,
                    contentDescription = "Zoomed Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.75f)
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .border(4.dp, sectionColor, RoundedCornerShape(16.dp))
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformState)
                )

                val infoText = if (currentImage.status == "STATUS_REJECTED") {
                    when {
                        currentImage.areEyesClosed == true -> "눈 감김"
                        currentImage.blurScore?.let { it < 30.0f } == true -> "흐릿함" // [FIX] Hardcoded constant
                        else -> "품질 저하"
                    }
                } else {
                    val nimaScore = (currentImage.nimaScore ?: 0.0) * 10
                    val smileProb = currentImage.smilingProbability ?: 0f
                    val smileBonus = if (smileProb < 0.1f) -10f else smileProb * 30f
                    val displayScore = (nimaScore + smileBonus).toInt().coerceAtLeast(1)
                    "${displayScore}점"
                }
                
                val showRestoreButton = currentSection == 2 && 
                                        (currentImage.areEyesClosed == false || currentImage.areEyesClosed == null) && 
                                        onRestoreClick != null

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showRestoreButton) {
                        Button(
                            onClick = { onRestoreClick?.invoke(currentImage) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진 심폐소생 (흔들림 복구)")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = infoText,
                            color = sectionColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 20.dp) 
                        .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = sectionTitle,
                        color = sectionColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val isAtStart = currentIndex == 0
                val isAtEnd = currentIndex == currentList.size - 1
                
                IconButton(
                    onClick = { if (!isAtStart) currentIndex-- },
                    enabled = !isAtStart,
                    modifier = Modifier.align(Alignment.CenterStart).padding(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Prev", tint = if (!isAtStart) Color.White else Color.White.copy(alpha=0.2f), modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(8.dp))
                }
                
                IconButton(
                    onClick = { if (!isAtEnd) currentIndex++ },
                    enabled = !isAtEnd,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Next", tint = if (!isAtEnd) Color.White else Color.White.copy(alpha=0.2f), modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(8.dp))
                }
                
                val prevSectionIndex = getPrevSection(currentSection)
                val nextSectionIndex = getNextSection(currentSection)

                if (prevSectionIndex != null) {
                    IconButton(
                        onClick = { currentSection = prevSectionIndex; currentIndex = 0 },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Prev Section", tint = Color.White, modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(4.dp))
                    }
                }

                if (nextSectionIndex != null) {
                    IconButton(
                        onClick = { currentSection = nextSectionIndex; currentIndex = 0 },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Section", tint = Color.White, modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CoachMark(text: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}
