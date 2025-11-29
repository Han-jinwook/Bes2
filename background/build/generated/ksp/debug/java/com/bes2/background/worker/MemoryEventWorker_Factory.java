package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.repository.GalleryRepository;
import com.bes2.ml.NimaQualityAnalyzer;
import com.bes2.ml.SmileDetector;
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
public final class MemoryEventWorker_Factory {
  private final Provider<GalleryRepository> galleryRepositoryProvider;

  private final Provider<ImageItemDao> imageItemDaoProvider;

  private final Provider<NimaQualityAnalyzer> nimaAnalyzerProvider;

  private final Provider<SmileDetector> smileDetectorProvider;

  public MemoryEventWorker_Factory(Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ImageItemDao> imageItemDaoProvider,
      Provider<NimaQualityAnalyzer> nimaAnalyzerProvider,
      Provider<SmileDetector> smileDetectorProvider) {
    this.galleryRepositoryProvider = galleryRepositoryProvider;
    this.imageItemDaoProvider = imageItemDaoProvider;
    this.nimaAnalyzerProvider = nimaAnalyzerProvider;
    this.smileDetectorProvider = smileDetectorProvider;
  }

  public MemoryEventWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, galleryRepositoryProvider.get(), imageItemDaoProvider.get(), nimaAnalyzerProvider.get(), smileDetectorProvider.get());
  }

  public static MemoryEventWorker_Factory create(
      Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<ImageItemDao> imageItemDaoProvider,
      Provider<NimaQualityAnalyzer> nimaAnalyzerProvider,
      Provider<SmileDetector> smileDetectorProvider) {
    return new MemoryEventWorker_Factory(galleryRepositoryProvider, imageItemDaoProvider, nimaAnalyzerProvider, smileDetectorProvider);
  }

  public static MemoryEventWorker newInstance(Context appContext, WorkerParameters workerParams,
      GalleryRepository galleryRepository, ImageItemDao imageItemDao,
      NimaQualityAnalyzer nimaAnalyzer, SmileDetector smileDetector) {
    return new MemoryEventWorker(appContext, workerParams, galleryRepository, imageItemDao, nimaAnalyzer, smileDetector);
  }
}
