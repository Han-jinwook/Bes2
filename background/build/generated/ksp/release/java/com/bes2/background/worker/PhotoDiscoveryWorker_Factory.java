package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ReviewItemDao;
import com.bes2.data.dao.TrashItemDao;
import com.bes2.data.repository.GalleryRepository;
import com.bes2.data.repository.SettingsRepository;
import com.bes2.ml.ImageContentClassifier;
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
public final class PhotoDiscoveryWorker_Factory {
  private final Provider<GalleryRepository> galleryRepositoryProvider;

  private final Provider<ReviewItemDao> reviewItemDaoProvider;

  private final Provider<TrashItemDao> trashItemDaoProvider;

  private final Provider<ImageContentClassifier> imageClassifierProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<WorkManager> workManagerProvider;

  public PhotoDiscoveryWorker_Factory(Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ReviewItemDao> reviewItemDaoProvider, Provider<TrashItemDao> trashItemDaoProvider,
      Provider<ImageContentClassifier> imageClassifierProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<WorkManager> workManagerProvider) {
    this.galleryRepositoryProvider = galleryRepositoryProvider;
    this.reviewItemDaoProvider = reviewItemDaoProvider;
    this.trashItemDaoProvider = trashItemDaoProvider;
    this.imageClassifierProvider = imageClassifierProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.workManagerProvider = workManagerProvider;
  }

  public PhotoDiscoveryWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, galleryRepositoryProvider.get(), reviewItemDaoProvider.get(), trashItemDaoProvider.get(), imageClassifierProvider.get(), settingsRepositoryProvider.get(), workManagerProvider.get());
  }

  public static PhotoDiscoveryWorker_Factory create(
      Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ReviewItemDao> reviewItemDaoProvider, Provider<TrashItemDao> trashItemDaoProvider,
      Provider<ImageContentClassifier> imageClassifierProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<WorkManager> workManagerProvider) {
    return new PhotoDiscoveryWorker_Factory(galleryRepositoryProvider, reviewItemDaoProvider, trashItemDaoProvider, imageClassifierProvider, settingsRepositoryProvider, workManagerProvider);
  }

  public static PhotoDiscoveryWorker newInstance(Context appContext, WorkerParameters workerParams,
      GalleryRepository galleryRepository, ReviewItemDao reviewItemDao, TrashItemDao trashItemDao,
      ImageContentClassifier imageClassifier, SettingsRepository settingsRepository,
      WorkManager workManager) {
    return new PhotoDiscoveryWorker(appContext, workerParams, galleryRepository, reviewItemDao, trashItemDao, imageClassifier, settingsRepository, workManager);
  }
}
