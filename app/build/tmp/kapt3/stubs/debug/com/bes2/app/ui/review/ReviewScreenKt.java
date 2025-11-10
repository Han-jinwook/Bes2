package com.bes2.app.ui.review;

import android.content.Context;
import android.provider.MediaStore;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.tooling.preview.Preview;
import androidx.navigation.NavController;
import com.bes2.background.worker.PhotoAnalysisWorker;
import com.bes2.data.model.ImageItemEntity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000P\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u001a\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001a\b\u0010\u000b\u001a\u00020\u0001H\u0007\u001a\b\u0010\f\u001a\u00020\u0001H\u0007\u001a`\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00112\u001e\u0010\u0012\u001a\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u0014\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u00132\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0018\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0007\u001a\b\u0010\u001c\u001a\u00020\u0001H\u0007\u001a,\u0010\u001d\u001a\u00020\u00012\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\b0\u00142\u0006\u0010\u001f\u001a\u00020 2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u00a8\u0006!"}, d2 = {"CoachMark", "", "text", "", "onDismiss", "Lkotlin/Function0;", "ImageWithInfo", "image", "Lcom/bes2/data/model/ImageItemEntity;", "modifier", "Landroidx/compose/ui/Modifier;", "LoadingState", "NoClustersState", "ReviewReadyState", "state", "Lcom/bes2/app/ui/review/ReviewUiState$Ready;", "onImageClick", "Lkotlin/Function1;", "onImageLongPress", "Lkotlin/Function2;", "", "onKeepSelected", "onDeleteOthers", "ReviewScreen", "viewModel", "Lcom/bes2/app/ui/review/ReviewViewModel;", "navController", "Landroidx/navigation/NavController;", "ReviewScreenPreview", "ZoomedImageDialog", "images", "initialIndex", "", "app_debug"})
public final class ReviewScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void ReviewScreen(@org.jetbrains.annotations.NotNull()
    com.bes2.app.ui.review.ReviewViewModel viewModel, @org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void LoadingState() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void NoClustersState() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ReviewReadyState(@org.jetbrains.annotations.NotNull()
    com.bes2.app.ui.review.ReviewUiState.Ready state, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.bes2.data.model.ImageItemEntity, kotlin.Unit> onImageClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.util.List<com.bes2.data.model.ImageItemEntity>, ? super com.bes2.data.model.ImageItemEntity, kotlin.Unit> onImageLongPress, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onKeepSelected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteOthers) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ImageWithInfo(@org.jetbrains.annotations.NotNull()
    com.bes2.data.model.ImageItemEntity image, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ZoomedImageDialog(@org.jetbrains.annotations.NotNull()
    java.util.List<com.bes2.data.model.ImageItemEntity> images, int initialIndex, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CoachMark(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
    @androidx.compose.runtime.Composable()
    public static final void ReviewScreenPreview() {
    }
}