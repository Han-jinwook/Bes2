package com.bes2.app.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bes2.app.ui.component.TypewriterText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 사진 검색", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.performSearch()
                        keyboardController?.hide()
                    }),
                    singleLine = true
                )
                
                // Overlay Placeholder with Animation if query is empty
                if (uiState.query.isEmpty()) {
                    val placeholders = listOf("예: 바닷가에서 웃는 아이", "예: 맛있는 파스타", "예: 푸른 하늘", "예: 생일 케이크")
                    Row(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(start = 56.dp), // Adjust for leading icon
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         TypewriterText(
                            texts = placeholders,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            typingDelay = 50L,
                            pauseDelay = 3000L
                        )
                    }
                }
            }
            
            // [Updated] Indexing & Status Indicator
            // Always show regardless of count to indicate system is alive
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val isIndexingComplete = uiState.totalImagesInDb > 0 && uiState.totalIndexedCount >= uiState.totalImagesInDb
                val isDbEmpty = uiState.totalImagesInDb == 0
                
                val icon = when {
                    isDbEmpty -> Icons.Default.HourglassEmpty
                    isIndexingComplete -> Icons.Default.CheckCircle
                    else -> Icons.Default.Sync
                }
                
                val iconColor = when {
                    isDbEmpty -> Color.Gray
                    isIndexingComplete -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = "Status",
                    modifier = Modifier.size(14.dp),
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                val statusText = when {
                    isDbEmpty -> "갤러리에서 사진을 불러오는 중입니다..."
                    isIndexingComplete -> "총 ${uiState.totalImagesInDb}장의 추억 속에서 검색합니다"
                    else -> "AI가 사진을 공부하는 중입니다... (${uiState.totalIndexedCount}/${uiState.totalImagesInDb})"
                }
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = iconColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.results.isEmpty() && uiState.query.isNotEmpty()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다.", color = Color.Gray)
                }
            } else {
                // Results Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 columns
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.results) { result ->
                        SearchResultItem(result)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(result: SearchResult) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
    ) {
        AsyncImage(
            model = result.image.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Similarity Score Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            val percentage = (result.score * 100).toInt().coerceAtLeast(0)
            Text(
                text = "${percentage}%",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Date Badge (Optional)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(4.dp)
        ) {
             val sdf = SimpleDateFormat("M.d", Locale.getDefault())
             Text(
                text = sdf.format(Date(result.image.timestamp)),
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}
