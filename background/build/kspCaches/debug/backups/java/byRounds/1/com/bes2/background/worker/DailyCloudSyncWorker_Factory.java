package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.repository.SettingsRepository;
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

  private final Provider<WorkManager> workManagerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public DailyCloudSyncWorker_Factory(Provider<ImageItemDao> imageItemDaoProvider,
      Provider<GooglePhotosProvider> googlePhotosProvider,
      Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.imageItemDaoProvider = imageItemDaoProvider;
    this.googlePhotosProvider = googlePhotosProvider;
    this.workManagerProvider = workManagerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public DailyCloudSyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, imageItemDaoProvider.get(), googlePhotosProvider.get(), workManagerProvider.get(), settingsRepositoryProvider.get());
  }

  public static DailyCloudSyncWorker_Factory create(Provider<ImageItemDao> imageItemDaoProvider,
      Provider<GooglePhotosProvider> googlePhotosProvider,
      Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DailyCloudSyncWorker_Factory(imageItemDaoProvider, googlePhotosProvider, workManagerProvider, settingsRepositoryProvider);
  }

  public static DailyCloudSyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      ImageItemDao imageItemDao, GooglePhotosProvider googlePhotosProvider, WorkManager workManager,
      SettingsRepository settingsRepository) {
    return new DailyCloudSyncWorker(appContext, workerParams, imageItemDao, googlePhotosProvider, workManager, settingsRepository);
  }
}
