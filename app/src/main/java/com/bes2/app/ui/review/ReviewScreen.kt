package com.bes2.app.ui.review

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReviewScreen(viewModel: ReviewViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel.navigationEvent) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                NavigationEvent.NavigateToHome -> {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                }
                else -> {}
            }
        }
    }

    var zoomedImageInfo by remember { mutableStateOf<Pair<List<ImageItemEntity>, Int>?>(null) }
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
            NoClustersState()
        }
        is ReviewUiState.Ready -> {
            state.pendingDeleteRequest?.let { urisToDelete ->
                LaunchedEffect(urisToDelete) {
                    val intentSender = MediaStore.createDeleteRequest(context.contentResolver, urisToDelete).intentSender
                    deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            }

            ReviewReadyState(
                state = state,
                onImageClick = { image -> viewModel.selectImage(image) },
                onImageLongPress = { list, image ->
                    val index = list.indexOf(image)
                    if (index != -1) {
                        zoomedImageInfo = list to index
                    }
                },
                onCompleteSelection = { viewModel.deleteOtherImages() }
            )

            if (zoomedImageInfo != null) {
                ZoomedImageDialog(
                    images = zoomedImageInfo!!.first,
                    initialIndex = zoomedImageInfo!!.second,
                    onDismiss = { zoomedImageInfo = null }
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "모든 사진 검토가 끝났습니다. 홈으로 이동합니다...")
    }
}

@Composable
fun ReviewReadyState(
    state: ReviewUiState.Ready,
    onImageClick: (ImageItemEntity) -> Unit,
    onImageLongPress: (List<ImageItemEntity>, ImageItemEntity) -> Unit,
    onCompleteSelection: () -> Unit
) {
    val bestImages = listOfNotNull(state.selectedBestImage, state.selectedSecondBestImage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("베스트 2장", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
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
                                onLongPress = { _ -> onImageLongPress(bestImages, image) }
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
                                onLongPress = { _ -> onImageLongPress(bestImages, image) }
                            )
                        }
                )
            } ?: Box(modifier = Modifier.weight(1f).fillMaxSize())
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("나머지 사진 (${state.otherImages.size}장)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.otherImages) { _, image ->
                ImageWithInfo(
                    image = image,
                    modifier = Modifier
                        .height(80.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(state.otherImages) {
                            detectTapGestures(
                                onTap = { _ -> onImageClick(image) },
                                onLongPress = { _ -> onImageLongPress(state.otherImages, image) }
                            )
                        }
                )
            }
        }

        if (state.rejectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("실패한 사진 (${state.rejectedImages.size}장)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.rejectedImages) { _, image ->
                    ImageWithInfo(
                        image = image,
                        modifier = Modifier
                            .height(80.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(state.rejectedImages) {
                                detectTapGestures(
                                    onLongPress = { _ -> onImageLongPress(state.rejectedImages, image) }
                                )
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCompleteSelection,
            modifier = Modifier.fillMaxWidth(),
            enabled = true 
        ) {
            Text("나머지/실패 사진 삭제하기")
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
            val smileBonus = (image.smilingProbability ?: 0f) * 20f
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
fun ZoomedImageDialog(
    images: List<ImageItemEntity>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var currentIndex by remember { mutableStateOf(initialIndex) }
        val currentImage = images[currentIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                offset += offsetChange
            }

            LaunchedEffect(currentIndex) {
                scale = 1f
                offset = Offset.Zero
            }

            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = currentImage.uri,
                    contentDescription = "Zoomed Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                )

                val infoText = if (currentImage.status == "STATUS_REJECTED") {
                    when {
                        currentImage.areEyesClosed == true -> "눈 감김"
                        currentImage.blurScore?.let { it < PhotoAnalysisWorker.BLUR_THRESHOLD } == true -> "흐릿함"
                        else -> "품질 저하"
                    }
                } else {
                    val nimaScore = (currentImage.nimaScore ?: 0f) * 10
                    val smileBonus = (currentImage.smilingProbability ?: 0f) * 20f
                    val displayScore = (nimaScore + smileBonus).toInt().coerceAtLeast(1)
                    "${displayScore}점"
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp) 
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = infoText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAtStart = currentIndex == 0
                    val isAtEnd = currentIndex == images.size - 1

                    IconButton(
                        onClick = { if (!isAtStart) currentIndex-- },
                        enabled = !isAtStart
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Previous Image",
                            tint = if (!isAtStart) Color.White else Color.Transparent,
                            modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(8.dp)
                        )
                    }
                    IconButton(
                        onClick = { if (!isAtEnd) currentIndex++ },
                        enabled = !isAtEnd
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Image",
                            tint = if (!isAtEnd) Color.White else Color.Transparent,
                            modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha=0.3f), CircleShape).padding(8.dp)
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

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    val fakeCluster = com.bes2.data.model.ImageClusterEntity(id=1, creationTime=0, reviewStatus="PENDING_REVIEW", bestImageUri="", secondBestImageUri=null)
    val fakeImages = listOf(
        ImageItemEntity(id = 1, uri = "", filePath = "", timestamp = 0, status = "ANALYZED", pHash = "1", nimaScore = 8.7f, blurScore = 90f, areEyesClosed = false, exposureScore = 0.5f, smilingProbability = 0.8f),
        ImageItemEntity(id = 2, uri = "", filePath = "", timestamp = 0, status = "ANALYZED", pHash = "2", nimaScore = 7.2f, blurScore = 80f, areEyesClosed = false, exposureScore = 0.6f, smilingProbability = 0.6f),
        ImageItemEntity(id = 3, uri = "", filePath = "", timestamp = 0, status = "ANALYZED", pHash = "3", nimaScore = 6.5f, blurScore = 70f, areEyesClosed = false, exposureScore = 0.7f, smilingProbability = 0.2f),
        ImageItemEntity(id = 4, uri = "", filePath = "", timestamp = 0, status = "STATUS_REJECTED", pHash = "4", nimaScore = 4.1f, blurScore = 20f, areEyesClosed = false, exposureScore = 0.8f, smilingProbability = 0.1f),
        ImageItemEntity(id = 5, uri = "", filePath = "", timestamp = 0, status = "STATUS_REJECTED", pHash = "5", nimaScore = 3.5f, blurScore = 60f, areEyesClosed = true, exposureScore = 0.9f, smilingProbability = 0.0f)
    )
    val readyState = ReviewUiState.Ready(
        cluster = fakeCluster,
        allImages = fakeImages,
        otherImages = listOf(fakeImages[2]),
        rejectedImages = listOf(fakeImages[3], fakeImages[4]),
        selectedBestImage = fakeImages[0],
        selectedSecondBestImage = fakeImages[1]
    )
    ReviewReadyState(
        state = readyState,
        onImageClick = {},
        onImageLongPress = { _, _ -> },
        onCompleteSelection = {}
    )
}
