package com.bes2.app.ui.review

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import com.bes2.background.util.ImageClusteringHelper
import com.bes2.data.dao.ImageClusterDao
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.model.ImageClusterEntity
import com.bes2.data.model.ReviewItemEntity
import com.bes2.data.repository.GalleryRepository
import com.bes2.data.repository.SettingsRepository
import com.bes2.data.repository.StoredSettings
import com.bes2.ml.ImageRestorationProcessor
import com.bes2.ml.NimaQualityAnalyzer
import com.bes2.ml.SmileDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private val imageClusterDao: ImageClusterDao = mock()
    private val reviewItemDao: ReviewItemDao = mock()
    private val galleryRepository: GalleryRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val workManager: WorkManager = mock()
    private val context: Context = mock()
    private val savedStateHandle: SavedStateHandle = mock()
    private val imageRestorationProcessor: ImageRestorationProcessor = mock()
    private val nimaAnalyzer: NimaQualityAnalyzer = mock()
    private val smileDetector: SmileDetector = mock()
    private val clusteringHelper: ImageClusteringHelper = mock()

    private lateinit var viewModel: ReviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mocks
        whenever(savedStateHandle.get<String>("source_type")).thenReturn("DIET")
        whenever(context.getSharedPreferences(any(), any())).thenReturn(mock())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `nextCluster finishes review when at last cluster`() = runTest(testDispatcher) {
        // Given: 1 cluster available
        val clusterId = "cluster1"
        val cluster = ImageClusterEntity(id = clusterId, creationTime = 100L, reviewStatus = "PENDING_REVIEW")
        val reviewItem = ReviewItemEntity(id = 1, uri = "uri1", filePath = "path1", timestamp = 100L, status = "CLUSTERED", cluster_id = clusterId, source_type = "DIET")
        val fakeSettings = StoredSettings(LocalTime.now(), "google_photos", true, "IMMEDIATE", 0, 5)

        whenever(imageClusterDao.getImageClustersByReviewStatus("PENDING_REVIEW"))
            .thenReturn(flowOf(listOf(cluster)))
        whenever(reviewItemDao.getItemsBySourceAndStatus("DIET", "CLUSTERED"))
            .thenReturn(listOf(reviewItem))
        whenever(imageClusterDao.getImageClusterById(clusterId))
            .thenReturn(flowOf(cluster))
        whenever(settingsRepository.storedSettings).thenReturn(flowOf(fakeSettings))

        // Initialize ViewModel
        viewModel = ReviewViewModel(
            imageClusterDao, reviewItemDao, galleryRepository, settingsRepository,
            workManager, context, savedStateHandle, imageRestorationProcessor,
            nimaAnalyzer, smileDetector, clusteringHelper
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial state loaded (Current Index = 0, Total = 1)
        // We are at the last cluster because Total is 1 and Index is 0.
        
        // When: nextCluster is called
        viewModel.nextCluster()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Should navigate to home (Finish Review)
        // Because index (0) was not < total - 1 (0). So it goes to else block.
        
        // Verify finishReview logic executed
        val currentState = viewModel.uiState.value
        assertTrue("State should be NoClustersToReview but was $currentState", currentState is ReviewUiState.NoClustersToReview)
    }
}
