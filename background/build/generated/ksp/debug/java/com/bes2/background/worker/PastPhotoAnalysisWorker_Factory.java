package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.repository.GalleryRepository;
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
public final class PastPhotoAnalysisWorker_Factory {
  private final Provider<GalleryRepository> galleryRepositoryProvider;

  private final Provider<ImageItemDao> imageDaoProvider;

  private final Provider<WorkManager> workManagerProvider;

  public PastPhotoAnalysisWorker_Factory(Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ImageItemDao> imageDaoProvider, Provider<WorkManager> workManagerProvider) {
    this.galleryRepositoryProvider = galleryRepositoryProvider;
    this.imageDaoProvider = imageDaoProvider;
    this.workManagerProvider = workManagerProvider;
  }

  public PastPhotoAnalysisWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, galleryRepositoryProvider.get(), imageDaoProvider.get(), workManagerProvider.get());
  }

  public static PastPhotoAnalysisWorker_Factory create(
      Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ImageItemDao> imageDaoProvider, Provider<WorkManager> workManagerProvider) {
    return new PastPhotoAnalysisWorker_Factory(galleryRepositoryProvider, imageDaoProvider, workManagerProvider);
  }

  public static PastPhotoAnalysisWorker newInstance(Context appContext,
      WorkerParameters workerParams, GalleryRepository galleryRepository, ImageItemDao imageDao,
      WorkManager workManager) {
    return new PastPhotoAnalysisWorker(appContext, workerParams, galleryRepository, imageDao, workManager);
  }
}
