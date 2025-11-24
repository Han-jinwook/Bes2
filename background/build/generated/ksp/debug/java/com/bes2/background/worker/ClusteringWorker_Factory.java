package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ImageClusterDao;
import com.bes2.data.dao.ImageItemDao;
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
public final class ClusteringWorker_Factory {
  private final Provider<ImageItemDao> imageDaoProvider;

  private final Provider<ImageClusterDao> imageClusterDaoProvider;

  private final Provider<WorkManager> workManagerProvider;

  private final Provider<ImageContentClassifier> imageContentClassifierProvider;

  public ClusteringWorker_Factory(Provider<ImageItemDao> imageDaoProvider,
      Provider<ImageClusterDao> imageClusterDaoProvider, Provider<WorkManager> workManagerProvider,
      Provider<ImageContentClassifier> imageContentClassifierProvider) {
    this.imageDaoProvider = imageDaoProvider;
    this.imageClusterDaoProvider = imageClusterDaoProvider;
    this.workManagerProvider = workManagerProvider;
    this.imageContentClassifierProvider = imageContentClassifierProvider;
  }

  public ClusteringWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, imageDaoProvider.get(), imageClusterDaoProvider.get(), workManagerProvider.get(), imageContentClassifierProvider.get());
  }

  public static ClusteringWorker_Factory create(Provider<ImageItemDao> imageDaoProvider,
      Provider<ImageClusterDao> imageClusterDaoProvider, Provider<WorkManager> workManagerProvider,
      Provider<ImageContentClassifier> imageContentClassifierProvider) {
    return new ClusteringWorker_Factory(imageDaoProvider, imageClusterDaoProvider, workManagerProvider, imageContentClassifierProvider);
  }

  public static ClusteringWorker newInstance(Context appContext, WorkerParameters workerParams,
      ImageItemDao imageDao, ImageClusterDao imageClusterDao, WorkManager workManager,
      ImageContentClassifier imageContentClassifier) {
    return new ClusteringWorker(appContext, workerParams, imageDao, imageClusterDao, workManager, imageContentClassifier);
  }
}
