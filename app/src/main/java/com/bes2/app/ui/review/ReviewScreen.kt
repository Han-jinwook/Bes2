package com.bes2.app.ui.review

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.bes2.app.MainActivity
import com.bes2.background.worker.PhotoAnalysisWorker
import com.bes2.data.model.ImageItemEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(viewModel: ReviewViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    fun navigateToHome() {
        val homeIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        context.startActivity(homeIntent)
    }

    LaunchedEffect(key1 = viewModel.navigationEvent) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToHome -> {
                    val message = "${event.clusterCount}개 묶음 중 베스트 ${event.savedCount}장을 정리했습니다."
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    delay(4000) 
                    navigateToHome()
                }
                else -> {}
            }
        }
    }

    // State to hold data for Zoomed Dialog: (BestList, OtherList, RejectedList, InitialSectionIndex, InitialImageIndex)
    // Section Index: 0=Best, 1=Other, 2=Rejected
    var zoomedImageState by remember { mutableStateOf<ZoomedImageState?>(null) }
    
    val prefs = remember { context.getSharedPreferences("bes2_prefs", Context.MODE_PRIVATE) }
    var showCoachMark by remember {
        val shouldShow = !prefs.getBoolean("coach_mark_shown_long_press", false)
        mutableStateOf(shouldShow)
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeletionRequestHandled(result.resultCode == AppCompatActivity.RESULT_OK)
    }

    when (val state = uiState) {
        is ReviewUiState.Loading -> {
            LoadingState()
        }
        is ReviewUiState.NoClustersToReview -> {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            LaunchedEffect(Unit) {
                navigateToHome()
            }
        }
        is ReviewUiState.Ready -> {
            state.pendingDeleteRequest?.let { urisToDelete ->
                LaunchedEffect(urisToDelete) {
                    val intentSender = MediaStore.createDeleteRequest(context.contentResolver, urisToDelete).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("사진 정리") },
                        navigationIcon = {
                            IconButton(onClick = { 
                                navigateToHome()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
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

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding() 
                                .height(60.dp) 
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "광고 영역 (AdMob Banner)",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    ReviewReadyState(
                        state = state,
                        onImageClick = { image -> viewModel.selectImage(image) },
                        onImageLongPress = { sectionIndex, index ->
                            val best = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage)
                            zoomedImageState = ZoomedImageState(
                                bestList = best,
                                otherList = state.otherImages,
                                rejectedList = state.rejectedImages,
                                initialSection = sectionIndex,
                                initialIndex = index
                            )
                        }
                    )
                }
            }

            zoomedImageState?.let {
                ZoomedImageDialogV2(
                    state = it,
                    onDismiss = { zoomedImageState = null }
                )
            }

            if (showCoachMark && (state.selectedBestImage != null || state.otherImages.isNotEmpty())) {
                CoachMark(
                    text = "사진을 길게 눌러\n크게 확인해보세요!",
                    onDismiss = {
                        prefs.edit().putBoolean("coach_mark_shown_long_press", true).apply()
                        showCoachMark = false
                    }
                )
            }
        }
    }
}

data class ZoomedImageState(
    val bestList: List<ImageItemEntity>,
    val otherList: List<ImageItemEntity>,
    val rejectedList: List<ImageItemEntity>,
    val initialSection: Int, // 0, 1, 2
    val initialIndex: Int
)

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "검토할 사진을 찾고 있습니다...")
    }
}

@Composable
fun NoClustersState() {
}

@Composable
fun ReviewReadyState(
    state: ReviewUiState.Ready,
    onImageClick: (ImageItemEntity) -> Unit,
    onImageLongPress: (Int, Int) -> Unit // sectionIndex, imageIndex
) {
    val bestImages = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage)

    // Colors
    val bestColor = Color(0xFF4CAF50) // Green
    val otherColor = Color(0xFFFFC107) // Amber
    val rejectedColor = Color(0xFFF44336) // Red

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("베스트 2장", style = MaterialTheme.typography.titleMedium, color = bestColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bestImages.getOrNull(0)?.let { image ->
                ImageWithInfo(
                    image = image,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(bestImages) {
                            detectTapGestures(
                                onTap = { _ -> onImageClick(image) },
                                onLongPress = { _ -> onImageLongPress(0, 0) }
                            )
                        }
                )
            }
            bestImages.getOrNull(1)?.let { image ->
                ImageWithInfo(
                    image = image,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(bestImages) {
                            detectTapGestures(
                                onTap = { _ -> onImageClick(image) },
                                onLongPress = { _ -> onImageLongPress(0, 1) }
                            )
                        }
                )
            } ?: Box(modifier = Modifier.weight(1f).fillMaxSize())
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("나머지 사진 (${state.otherImages.size}장)", style = MaterialTheme.typography.titleMedium, color = otherColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.otherImages) { index, image ->
                ImageWithInfo(
                    image = image,
                    modifier = Modifier
                        .height(80.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(state.otherImages) {
                            detectTapGestures(
                                onTap = { _ -> onImageClick(image) },
                                onLongPress = { _ -> onImageLongPress(1, index) }
                            )
                        }
                )
            }
        }

        if (state.rejectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("실패한 사진 (${state.rejectedImages.size}장)", style = MaterialTheme.typography.titleMedium, color = rejectedColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.rejectedImages) { index, image ->
                    ImageWithInfo(
                        image = image,
                        modifier = Modifier
                            .height(80.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(state.rejectedImages) {
                                detectTapGestures(
                                    onLongPress = { _ -> onImageLongPress(2, index) }
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun ImageWithInfo(
    image: ImageItemEntity,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = image.uri,
            contentDescription = "Image ${image.id}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val infoText = if (image.status == "STATUS_REJECTED") {
            when {
                image.areEyesClosed == true -> "눈 감김"
                image.blurScore?.let { it < PhotoAnalysisWorker.BLUR_THRESHOLD } == true -> "흐릿함"
                else -> "품질 저하"
            }
        } else {
            val nimaScore = (image.nimaScore ?: 0f) * 10
            val smileProb = image.smilingProbability ?: 0f
            val smileBonus = if (smileProb < 0.1f) -10f else smileProb * 30f
            val displayScore = (nimaScore + smileBonus).toInt().coerceAtLeast(1)
            "${displayScore}점"
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = infoText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ZoomedImageDialogV2(
    state: ZoomedImageState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var currentSection by remember { mutableStateOf(state.initialSection) }
        var currentIndex by remember { mutableStateOf(state.initialIndex) }

        val currentList = when (currentSection) {
            0 -> state.bestList
            1 -> state.otherList
            2 -> state.rejectedList
            else -> emptyList()
        }
        
        // Safety check: if list is empty, dismiss or handle
        if (currentList.isEmpty()) {
            LaunchedEffect(Unit) { onDismiss() }
            return@Dialog
        }
        
        val currentImage = currentList.getOrNull(currentIndex) ?: currentList[0]

        // Colors & Labels based on section
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

            // Image Container with colored border
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
                        .border(4.dp, sectionColor, RoundedCornerShape(16.dp)) // Colored Border
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformState)
                )

                // Info Label
                val infoText = if (currentImage.status == "STATUS_REJECTED") {
                    when {
                        currentImage.areEyesClosed == true -> "눈 감김"
                        currentImage.blurScore?.let { it < PhotoAnalysisWorker.BLUR_THRESHOLD } == true -> "흐릿함"
                        else -> "품질 저하"
                    }
                } else {
                    val nimaScore = (currentImage.nimaScore ?: 0f) * 10
                    val smileProb = currentImage.smilingProbability ?: 0f
                    val smileBonus = if (smileProb < 0.1f) -10f else smileProb * 30f
                    val displayScore = (nimaScore + smileBonus).toInt().coerceAtLeast(1)
                    "${displayScore}점"
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp) 
                        .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = infoText,
                        color = sectionColor, // Match text color with section color
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Top Label (Section Name)
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

            // Navigation Buttons
            Box(modifier = Modifier.fillMaxSize()) {
                // Left/Right
                val isAtStart = currentIndex == 0
                val isAtEnd = currentIndex == currentList.size - 1
                
                // Left
                IconButton(
                    onClick = { 
                        if (!isAtStart) currentIndex--
                    },
                    enabled = !isAtStart,
                    modifier = Modifier.align(Alignment.CenterStart).padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Prev",
                        tint = if (!isAtStart) Color.White else Color.White.copy(alpha=0.2f),
                        modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(8.dp)
                    )
                }
                
                // Right
                IconButton(
                    onClick = { 
                        if (!isAtEnd) currentIndex++
                    },
                    enabled = !isAtEnd,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Next",
                        tint = if (!isAtEnd) Color.White else Color.White.copy(alpha=0.2f),
                        modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(8.dp)
                    )
                }
                
                // Up/Down (Section Jump)
                // Only show if prev/next section exists and has items
                val hasPrevSection = currentSection > 0 && when(currentSection-1) {
                    0 -> state.bestList.isNotEmpty()
                    1 -> state.otherList.isNotEmpty()
                    else -> false
                }
                val hasNextSection = currentSection < 2 && when(currentSection+1) {
                    1 -> state.otherList.isNotEmpty()
                    2 -> state.rejectedList.isNotEmpty()
                    else -> false
                }

                // Up (Previous Section)
                if (hasPrevSection) {
                    IconButton(
                        onClick = { 
                            currentSection--
                            currentIndex = 0 
                        },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Prev Section",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(4.dp)
                        )
                    }
                }

                // Down (Next Section)
                if (hasNextSection) {
                    IconButton(
                        onClick = { 
                            currentSection++
                            currentIndex = 0
                        },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Next Section",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(4.dp)
                        )
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
