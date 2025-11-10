package com.bes2.app.ui.review;

import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.bes2.data.dao.ImageClusterDao;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.model.ImageClusterEntity;
import com.bes2.data.model.ImageItemEntity;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.flow.SharedFlow;
import kotlinx.coroutines.flow.StateFlow;
import timber.log.Timber;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J2\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00180\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010\u00182\b\u0010 \u001a\u0004\u0018\u00010\u0018H\u0002J\u0006\u0010!\u001a\u00020\"J\u0006\u0010#\u001a\u00020\"J\u000e\u0010$\u001a\u00020\"2\u0006\u0010%\u001a\u00020&J\u000e\u0010\'\u001a\u00020\"2\u0006\u0010(\u001a\u00020\u0018R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\f0\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006)"}, d2 = {"Lcom/bes2/app/ui/review/ReviewViewModel;", "Landroidx/lifecycle/ViewModel;", "imageClusterDao", "Lcom/bes2/data/dao/ImageClusterDao;", "imageItemDao", "Lcom/bes2/data/dao/ImageItemDao;", "(Lcom/bes2/data/dao/ImageClusterDao;Lcom/bes2/data/dao/ImageItemDao;)V", "_navigationEvent", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/bes2/app/ui/review/NavigationEvent;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/bes2/app/ui/review/ReviewUiState;", "navigationEvent", "Lkotlinx/coroutines/flow/SharedFlow;", "getNavigationEvent", "()Lkotlinx/coroutines/flow/SharedFlow;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "calculateFinalScore", "", "image", "Lcom/bes2/data/model/ImageItemEntity;", "calculateReadyState", "Lcom/bes2/app/ui/review/ReviewUiState$Ready;", "cluster", "Lcom/bes2/data/model/ImageClusterEntity;", "allImages", "", "newFirst", "newSecond", "deleteOtherImages", "", "keepSelectedImages", "onDeletionRequestHandled", "successfullyDeleted", "", "selectImage", "imageToSelect", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
public final class ReviewViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.bes2.data.dao.ImageClusterDao imageClusterDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.bes2.data.dao.ImageItemDao imageItemDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.bes2.app.ui.review.ReviewUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.bes2.app.ui.review.ReviewUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.bes2.app.ui.review.NavigationEvent> _navigationEvent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.bes2.app.ui.review.NavigationEvent> navigationEvent = null;
    
    @javax.inject.Inject()
    public ReviewViewModel(@org.jetbrains.annotations.NotNull()
    com.bes2.data.dao.ImageClusterDao imageClusterDao, @org.jetbrains.annotations.NotNull()
    com.bes2.data.dao.ImageItemDao imageItemDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.bes2.app.ui.review.ReviewUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.bes2.app.ui.review.NavigationEvent> getNavigationEvent() {
        return null;
    }
    
    public final void selectImage(@org.jetbrains.annotations.NotNull()
    com.bes2.data.model.ImageItemEntity imageToSelect) {
    }
    
    private final float calculateFinalScore(com.bes2.data.model.ImageItemEntity image) {
        return 0.0F;
    }
    
    private final com.bes2.app.ui.review.ReviewUiState.Ready calculateReadyState(com.bes2.data.model.ImageClusterEntity cluster, java.util.List<com.bes2.data.model.ImageItemEntity> allImages, com.bes2.data.model.ImageItemEntity newFirst, com.bes2.data.model.ImageItemEntity newSecond) {
        return null;
    }
    
    public final void keepSelectedImages() {
    }
    
    public final void deleteOtherImages() {
    }
    
    public final void onDeletionRequestHandled(boolean successfullyDeleted) {
    }
}