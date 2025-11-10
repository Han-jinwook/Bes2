package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ImageItemDao;
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
  private final Provider<ImageItemDao> imageItemDaoProvider;

  private final Provider<GooglePhotosProvider> googlePhotosProvider;

  public DailyCloudSyncWorker_Factory(Provider<ImageItemDao> imageItemDaoProvider,
      Provider<GooglePhotosProvider> googlePhotosProvider) {
    this.imageItemDaoProvider = imageItemDaoProvider;
    this.googlePhotosProvider = googlePhotosProvider;
  }

  public DailyCloudSyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, imageItemDaoProvider.get(), googlePhotosProvider.get());
  }

  public static DailyCloudSyncWorker_Factory create(Provider<ImageItemDao> imageItemDaoProvider,
      Provider<GooglePhotosProvider> googlePhotosProvider) {
    return new DailyCloudSyncWorker_Factory(imageItemDaoProvider, googlePhotosProvider);
  }

  public static DailyCloudSyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      ImageItemDao imageItemDao, GooglePhotosProvider googlePhotosProvider) {
    return new DailyCloudSyncWorker(appContext, workerParams, imageItemDao, googlePhotosProvider);
  }
}
