package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ReviewItemDao;
import com.bes2.photos_integration.google.GooglePhotosProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DailyCloudSyncWorker_Factory {
  private final Provider<ReviewItemDao> reviewItemDaoProvider;

  private final Provider<GooglePhotosProvider> googlePhotosProvider;

  public DailyCloudSyncWorker_Factory(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<GooglePhotosProvider> googlePhotosProvider) {
    this.reviewItemDaoProvider = reviewItemDaoProvider;
    this.googlePhotosProvider = googlePhotosProvider;
  }

  public DailyCloudSyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, reviewItemDaoProvider.get(), googlePhotosProvider.get());
  }

  public static DailyCloudSyncWorker_Factory create(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<GooglePhotosProvider> googlePhotosProvider) {
    return new DailyCloudSyncWorker_Factory(reviewItemDaoProvider, googlePhotosProvider);
  }

  public static DailyCloudSyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      ReviewItemDao reviewItemDao, GooglePhotosProvider googlePhotosProvider) {
    return new DailyCloudSyncWorker(appContext, workerParams, reviewItemDao, googlePhotosProvider);
  }
}
