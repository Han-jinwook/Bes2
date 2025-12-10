package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ReviewItemDao;
import com.bes2.data.repository.SettingsRepository;
import com.bes2.ml.BacklightingDetector;
import com.bes2.ml.EyeClosedDetector;
import com.bes2.ml.FaceEmbedder;
import com.bes2.ml.ImageContentClassifier;
import com.bes2.ml.MusiqQualityAnalyzer;
import com.bes2.ml.NimaQualityAnalyzer;
import com.bes2.ml.SemanticSearchEngine;
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
  private final Provider<ReviewItemDao> reviewItemDaoProvider;

  private final Provider<NimaQualityAnalyzer> nimaAnalyzerProvider;

  private final Provider<MusiqQualityAnalyzer> musiqAnalyzerProvider;

  private final Provider<EyeClosedDetector> eyeClosedDetectorProvider;

  private final Provider<BacklightingDetector> backlightingDetectorProvider;

  private final Provider<FaceEmbedder> faceEmbedderProvider;

  private final Provider<SmileDetector> smileDetectorProvider;

  private final Provider<SemanticSearchEngine> semanticSearchEngineProvider;

  private final Provider<ImageContentClassifier> imageClassifierProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public PhotoAnalysisWorker_Factory(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<NimaQualityAnalyzer> nimaAnalyzerProvider,
      Provider<MusiqQualityAnalyzer> musiqAnalyzerProvider,
      Provider<EyeClosedDetector> eyeClosedDetectorProvider,
      Provider<BacklightingDetector> backlightingDetectorProvider,
      Provider<FaceEmbedder> faceEmbedderProvider, Provider<SmileDetector> smileDetectorProvider,
      Provider<SemanticSearchEngine> semanticSearchEngineProvider,
      Provider<ImageContentClassifier> imageClassifierProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.reviewItemDaoProvider = reviewItemDaoProvider;
    this.nimaAnalyzerProvider = nimaAnalyzerProvider;
    this.musiqAnalyzerProvider = musiqAnalyzerProvider;
    this.eyeClosedDetectorProvider = eyeClosedDetectorProvider;
    this.backlightingDetectorProvider = backlightingDetectorProvider;
    this.faceEmbedderProvider = faceEmbedderProvider;
    this.smileDetectorProvider = smileDetectorProvider;
    this.semanticSearchEngineProvider = semanticSearchEngineProvider;
    this.imageClassifierProvider = imageClassifierProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public PhotoAnalysisWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, reviewItemDaoProvider.get(), nimaAnalyzerProvider.get(), musiqAnalyzerProvider.get(), eyeClosedDetectorProvider.get(), backlightingDetectorProvider.get(), faceEmbedderProvider.get(), smileDetectorProvider.get(), semanticSearchEngineProvider.get(), imageClassifierProvider.get(), settingsRepositoryProvider.get());
  }

  public static PhotoAnalysisWorker_Factory create(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<NimaQualityAnalyzer> nimaAnalyzerProvider,
      Provider<MusiqQualityAnalyzer> musiqAnalyzerProvider,
      Provider<EyeClosedDetector> eyeClosedDetectorProvider,
      Provider<BacklightingDetector> backlightingDetectorProvider,
      Provider<FaceEmbedder> faceEmbedderProvider, Provider<SmileDetector> smileDetectorProvider,
      Provider<SemanticSearchEngine> semanticSearchEngineProvider,
      Provider<ImageContentClassifier> imageClassifierProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new PhotoAnalysisWorker_Factory(reviewItemDaoProvider, nimaAnalyzerProvider, musiqAnalyzerProvider, eyeClosedDetectorProvider, backlightingDetectorProvider, faceEmbedderProvider, smileDetectorProvider, semanticSearchEngineProvider, imageClassifierProvider, settingsRepositoryProvider);
  }

  public static PhotoAnalysisWorker newInstance(Context appContext, WorkerParameters workerParams,
      ReviewItemDao reviewItemDao, NimaQualityAnalyzer nimaAnalyzer,
      MusiqQualityAnalyzer musiqAnalyzer, EyeClosedDetector eyeClosedDetector,
      BacklightingDetector backlightingDetector, FaceEmbedder faceEmbedder,
      SmileDetector smileDetector, SemanticSearchEngine semanticSearchEngine,
      ImageContentClassifier imageClassifier, SettingsRepository settingsRepository) {
    return new PhotoAnalysisWorker(appContext, workerParams, reviewItemDao, nimaAnalyzer, musiqAnalyzer, eyeClosedDetector, backlightingDetector, faceEmbedder, smileDetector, semanticSearchEngine, imageClassifier, settingsRepository);
  }
}
