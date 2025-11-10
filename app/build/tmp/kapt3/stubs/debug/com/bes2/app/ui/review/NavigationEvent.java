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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0010\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\bv\u0018\u00002\u00020\u0001:\u0001\u0002\u0082\u0001\u0001\u0003\u00a8\u0006\u0004"}, d2 = {"Lcom/bes2/app/ui/review/NavigationEvent;", "", "NavigateToSettings", "Lcom/bes2/app/ui/review/NavigationEvent$NavigateToSettings;", "app_debug"})
public abstract interface NavigationEvent {
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/bes2/app/ui/review/NavigationEvent$NavigateToSettings;", "Lcom/bes2/app/ui/review/NavigationEvent;", "()V", "app_debug"})
    public static final class NavigateToSettings implements com.bes2.app.ui.review.NavigationEvent {
        @org.jetbrains.annotations.NotNull()
        public static final com.bes2.app.ui.review.NavigationEvent.NavigateToSettings INSTANCE = null;
        
        private NavigateToSettings() {
            super();
        }
    }
}