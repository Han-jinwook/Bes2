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

  private final Provider<WorkManager> workManagerProvider;

  private final Provider<ImageContentClassifier> imageClassifierProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public PhotoDiscoveryWorker_Factory(Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ReviewItemDao> reviewItemDaoProvider, Provider<TrashItemDao> trashItemDaoProvider,
      Provider<WorkManager> workManagerProvider,
      Provider<ImageContentClassifier> imageClassifierProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.galleryRepositoryProvider = galleryRepositoryProvider;
    this.reviewItemDaoProvider = reviewItemDaoProvider;
    this.trashItemDaoProvider = trashItemDaoProvider;
    this.workManagerProvider = workManagerProvider;
    this.imageClassifierProvider = imageClassifierProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public PhotoDiscoveryWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, galleryRepositoryProvider.get(), reviewItemDaoProvider.get(), trashItemDaoProvider.get(), workManagerProvider.get(), imageClassifierProvider.get(), settingsRepositoryProvider.get());
  }

  public static PhotoDiscoveryWorker_Factory create(
      Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ReviewItemDao> reviewItemDaoProvider, Provider<TrashItemDao> trashItemDaoProvider,
      Provider<WorkManager> workManagerProvider,
      Provider<ImageContentClassifier> imageClassifierProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new PhotoDiscoveryWorker_Factory(galleryRepositoryProvider, reviewItemDaoProvider, trashItemDaoProvider, workManagerProvider, imageClassifierProvider, settingsRepositoryProvider);
  }

  public static PhotoDiscoveryWorker newInstance(Context appContext, WorkerParameters workerParams,
      GalleryRepository galleryRepository, ReviewItemDao reviewItemDao, TrashItemDao trashItemDao,
      WorkManager workManager, ImageContentClassifier imageClassifier,
      SettingsRepository settingsRepository) {
    return new PhotoDiscoveryWorker(appContext, workerParams, galleryRepository, reviewItemDao, trashItemDao, workManager, imageClassifier, settingsRepository);
  }
}
