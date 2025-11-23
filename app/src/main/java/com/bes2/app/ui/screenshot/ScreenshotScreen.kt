package com.bes2.app.ui.screenshot

import android.app.Activity
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.bes2.data.model.ScreenshotItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotScreen(
    viewModel: ScreenshotViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeleteCompleted(result.resultCode == Activity.RESULT_OK)
    }

    // Handle Result Message (Toast) - State based
    LaunchedEffect(uiState.resultMessage) {
        uiState.resultMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.messageShown()
        }
    }

    LaunchedEffect(uiState.pendingDeleteUris) {
        uiState.pendingDeleteUris?.let { uris ->
            val intentSender = MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
    }

    // Zoom state
    var zoomedImageInfo by remember { mutableStateOf<Pair<List<ScreenshotItem>, Int>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스크린샷 청소") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isAllSelected = uiState.screenshots.isNotEmpty() && uiState.screenshots.all { it.isSelected }
                    TextButton(onClick = { viewModel.toggleAllSelection(!isAllSelected) }) {
                        Text(if (isAllSelected) "선택 해제" else "전체 선택")
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
                // Action Buttons Bar (Moved to Bottom)
                val selectedCount = uiState.screenshots.count { it.isSelected }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.keepSelected() },
                        modifier = Modifier.weight(1f).height(56.dp), // Increased height for better touch target
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("보관하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { viewModel.deleteSelected() },
                        modifier = Modifier.weight(1f).height(56.dp), // Increased height for better touch target
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedCount > 0) "${selectedCount}장 삭제" else "삭제하기",
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // AdMob Placeholder
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.screenshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("청소할 스크린샷이 없습니다! ✨", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(uiState.screenshots) { index, item ->
                    ScreenshotGridItem(
                        item = item,
                        onToggle = { viewModel.toggleSelection(item) },
                        onLongPress = { zoomedImageInfo = uiState.screenshots to index }
                    )
                }
            }
        }
    }

    if (zoomedImageInfo != null) {
        ZoomedScreenshotDialog(
            images = zoomedImageInfo!!.first,
            initialIndex = zoomedImageInfo!!.second,
            onDismiss = { zoomedImageInfo = null }
        )
    }
}

@Composable
fun ScreenshotGridItem(
    item: ScreenshotItem,
    onToggle: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggle() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        if (item.isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        Icon(
            imageVector = if (item.isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = "Select",
            tint = if (item.isSelected) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )
    }
}

@Composable
fun ZoomedScreenshotDialog(
    images: List<ScreenshotItem>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var currentIndex by remember { mutableStateOf(initialIndex) }
        val currentImage = images[currentIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Make background transparent to see behind content
                .background(Color.Transparent)
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
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight(0.75f)
                        // Add shadow for separation
                        .shadow(12.dp, RoundedCornerShape(16.dp))
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
                            tint = if (!isAtStart) Color.White else Color.Black.copy(alpha=0.5f),
                            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha=0.7f), CircleShape).padding(8.dp)
                        )
                    }
                    IconButton(
                        onClick = { if (!isAtEnd) currentIndex++ },
                        enabled = !isAtEnd
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Image",
                            tint = if (!isAtEnd) Color.White else Color.Black.copy(alpha=0.5f),
                            modifier = Modifier.size(48.dp).background(Color.White.copy(alpha=0.7f), CircleShape).padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
