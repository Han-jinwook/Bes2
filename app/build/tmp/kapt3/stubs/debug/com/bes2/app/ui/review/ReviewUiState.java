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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\bv\u0018\u00002\u00020\u0001:\u0003\u0002\u0003\u0004\u0082\u0001\u0003\u0005\u0006\u0007\u00a8\u0006\b"}, d2 = {"Lcom/bes2/app/ui/review/ReviewUiState;", "", "Loading", "NoClustersToReview", "Ready", "Lcom/bes2/app/ui/review/ReviewUiState$Loading;", "Lcom/bes2/app/ui/review/ReviewUiState$NoClustersToReview;", "Lcom/bes2/app/ui/review/ReviewUiState$Ready;", "app_debug"})
public abstract interface ReviewUiState {
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/bes2/app/ui/review/ReviewUiState$Loading;", "Lcom/bes2/app/ui/review/ReviewUiState;", "()V", "app_debug"})
    public static final class Loading implements com.bes2.app.ui.review.ReviewUiState {
        @org.jetbrains.annotations.NotNull()
        public static final com.bes2.app.ui.review.ReviewUiState.Loading INSTANCE = null;
        
        private Loading() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/bes2/app/ui/review/ReviewUiState$NoClustersToReview;", "Lcom/bes2/app/ui/review/ReviewUiState;", "()V", "app_debug"})
    public static final class NoClustersToReview implements com.bes2.app.ui.review.ReviewUiState {
        @org.jetbrains.annotations.NotNull()
        public static final com.bes2.app.ui.review.ReviewUiState.NoClustersToReview INSTANCE = null;
        
        private NoClustersToReview() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B]\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0006\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u0006\u0012\u0010\b\u0002\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u0011\u0010\u001e\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u0005H\u00c6\u0003Jm\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00062\u0010\b\u0002\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u00d6\u0003J\t\u0010$\u001a\u00020%H\u00d6\u0001J\t\u0010&\u001a\u00020\'H\u00d6\u0001R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u0019\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000fR\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000fR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0016\u00a8\u0006("}, d2 = {"Lcom/bes2/app/ui/review/ReviewUiState$Ready;", "Lcom/bes2/app/ui/review/ReviewUiState;", "cluster", "Lcom/bes2/data/model/ImageClusterEntity;", "allImages", "", "Lcom/bes2/data/model/ImageItemEntity;", "otherImages", "rejectedImages", "selectedBestImage", "selectedSecondBestImage", "pendingDeleteRequest", "Landroid/net/Uri;", "(Lcom/bes2/data/model/ImageClusterEntity;Ljava/util/List;Ljava/util/List;Ljava/util/List;Lcom/bes2/data/model/ImageItemEntity;Lcom/bes2/data/model/ImageItemEntity;Ljava/util/List;)V", "getAllImages", "()Ljava/util/List;", "getCluster", "()Lcom/bes2/data/model/ImageClusterEntity;", "getOtherImages", "getPendingDeleteRequest", "getRejectedImages", "getSelectedBestImage", "()Lcom/bes2/data/model/ImageItemEntity;", "getSelectedSecondBestImage", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Ready implements com.bes2.app.ui.review.ReviewUiState {
        @org.jetbrains.annotations.NotNull()
        private final com.bes2.data.model.ImageClusterEntity cluster = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.bes2.data.model.ImageItemEntity> allImages = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.bes2.data.model.ImageItemEntity> otherImages = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.bes2.data.model.ImageItemEntity> rejectedImages = null;
        @org.jetbrains.annotations.Nullable()
        private final com.bes2.data.model.ImageItemEntity selectedBestImage = null;
        @org.jetbrains.annotations.Nullable()
        private final com.bes2.data.model.ImageItemEntity selectedSecondBestImage = null;
        @org.jetbrains.annotations.Nullable()
        private final java.util.List<android.net.Uri> pendingDeleteRequest = null;
        
        public Ready(@org.jetbrains.annotations.NotNull()
        com.bes2.data.model.ImageClusterEntity cluster, @org.jetbrains.annotations.NotNull()
        java.util.List<com.bes2.data.model.ImageItemEntity> allImages, @org.jetbrains.annotations.NotNull()
        java.util.List<com.bes2.data.model.ImageItemEntity> otherImages, @org.jetbrains.annotations.NotNull()
        java.util.List<com.bes2.data.model.ImageItemEntity> rejectedImages, @org.jetbrains.annotations.Nullable()
        com.bes2.data.model.ImageItemEntity selectedBestImage, @org.jetbrains.annotations.Nullable()
        com.bes2.data.model.ImageItemEntity selectedSecondBestImage, @org.jetbrains.annotations.Nullable()
        java.util.List<? extends android.net.Uri> pendingDeleteRequest) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.bes2.data.model.ImageClusterEntity getCluster() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.bes2.data.model.ImageItemEntity> getAllImages() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.bes2.data.model.ImageItemEntity> getOtherImages() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.bes2.data.model.ImageItemEntity> getRejectedImages() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.bes2.data.model.ImageItemEntity getSelectedBestImage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.bes2.data.model.ImageItemEntity getSelectedSecondBestImage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<android.net.Uri> getPendingDeleteRequest() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.bes2.data.model.ImageClusterEntity component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.bes2.data.model.ImageItemEntity> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.bes2.data.model.ImageItemEntity> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.bes2.data.model.ImageItemEntity> component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.bes2.data.model.ImageItemEntity component5() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.bes2.data.model.ImageItemEntity component6() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<android.net.Uri> component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.bes2.app.ui.review.ReviewUiState.Ready copy(@org.jetbrains.annotations.NotNull()
        com.bes2.data.model.ImageClusterEntity cluster, @org.jetbrains.annotations.NotNull()
        java.util.List<com.bes2.data.model.ImageItemEntity> allImages, @org.jetbrains.annotations.NotNull()
        java.util.List<com.bes2.data.model.ImageItemEntity> otherImages, @org.jetbrains.annotations.NotNull()
        java.util.List<com.bes2.data.model.ImageItemEntity> rejectedImages, @org.jetbrains.annotations.Nullable()
        com.bes2.data.model.ImageItemEntity selectedBestImage, @org.jetbrains.annotations.Nullable()
        com.bes2.data.model.ImageItemEntity selectedSecondBestImage, @org.jetbrains.annotations.Nullable()
        java.util.List<? extends android.net.Uri> pendingDeleteRequest) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}