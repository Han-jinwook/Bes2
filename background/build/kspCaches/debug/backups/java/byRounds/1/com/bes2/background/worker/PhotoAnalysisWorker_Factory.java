package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.bes2.core_common.provider.ResourceProvider;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.ml.EyeClosedDetector;
import com.bes2.ml.FaceEmbedder;
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
public final class PhotoAnalysisWorker_Factory {
  private final Provider<ImageItemDao> imageDaoProvider;

  private final Provider<WorkManager> workManagerProvider;

  private final Provider<NimaQualityAnalyzer> nimaAnalyzerProvider;

  private final Provider<EyeClosedDetector> eyeClosedDetectorProvider;

  private final Provider<FaceEmbedder> faceEmbedderProvider;

  private final Provider<SmileDetector> smileDetectorProvider;

  private final Provider<ResourceProvider> resourceProvider;

  public PhotoAnalysisWorker_Factory(Provider<ImageItemDao> imageDaoProvider,
      Provider<WorkManager> workManagerProvider, Provider<NimaQualityAnalyzer> nimaAnalyzerProvider,
      Provider<EyeClosedDetector> eyeClosedDetectorProvider,
      Provider<FaceEmbedder> faceEmbedderProvider, Provider<SmileDetector> smileDetectorProvider,
      Provider<ResourceProvider> resourceProvider) {
    this.imageDaoProvider = imageDaoProvider;
    this.workManagerProvider = workManagerProvider;
    this.nimaAnalyzerProvider = nimaAnalyzerProvider;
    this.eyeClosedDetectorProvider = eyeClosedDetectorProvider;
    this.faceEmbedderProvider = faceEmbedderProvider;
    this.smileDetectorProvider = smileDetectorProvider;
    this.resourceProvider = resourceProvider;
  }

  public PhotoAnalysisWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, imageDaoProvider.get(), workManagerProvider.get(), nimaAnalyzerProvider.get(), eyeClosedDetectorProvider.get(), faceEmbedderProvider.get(), smileDetectorProvider.get(), resourceProvider.get());
  }

  public static PhotoAnalysisWorker_Factory create(Provider<ImageItemDao> imageDaoProvider,
      Provider<WorkManager> workManagerProvider, Provider<NimaQualityAnalyzer> nimaAnalyzerProvider,
      Provider<EyeClosedDetector> eyeClosedDetectorProvider,
      Provider<FaceEmbedder> faceEmbedderProvider, Provider<SmileDetector> smileDetectorProvider,
      Provider<ResourceProvider> resourceProvider) {
    return new PhotoAnalysisWorker_Factory(imageDaoProvider, workManagerProvider, nimaAnalyzerProvider, eyeClosedDetectorProvider, faceEmbedderProvider, smileDetectorProvider, resourceProvider);
  }

  public static PhotoAnalysisWorker newInstance(Context appContext, WorkerParameters workerParams,
      ImageItemDao imageDao, WorkManager workManager, NimaQualityAnalyzer nimaAnalyzer,
      EyeClosedDetector eyeClosedDetector, FaceEmbedder faceEmbedder, SmileDetector smileDetector,
      ResourceProvider resourceProvider) {
    return new PhotoAnalysisWorker(appContext, workerParams, imageDao, workManager, nimaAnalyzer, eyeClosedDetector, faceEmbedder, smileDetector, resourceProvider);
  }
}
