package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.background.util.ImageClusteringHelper;
import com.bes2.data.dao.ImageClusterDao;
import com.bes2.data.dao.ReviewItemDao;
import com.bes2.data.repository.SettingsRepository;
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
  private final Provider<ReviewItemDao> reviewItemDaoProvider;

  private final Provider<ImageClusterDao> imageClusterDaoProvider;

  private final Provider<ImageClusteringHelper> clusteringHelperProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public ClusteringWorker_Factory(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<ImageClusterDao> imageClusterDaoProvider,
      Provider<ImageClusteringHelper> clusteringHelperProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.reviewItemDaoProvider = reviewItemDaoProvider;
    this.imageClusterDaoProvider = imageClusterDaoProvider;
    this.clusteringHelperProvider = clusteringHelperProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public ClusteringWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, reviewItemDaoProvider.get(), imageClusterDaoProvider.get(), clusteringHelperProvider.get(), settingsRepositoryProvider.get());
  }

  public static ClusteringWorker_Factory create(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<ImageClusterDao> imageClusterDaoProvider,
      Provider<ImageClusteringHelper> clusteringHelperProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new ClusteringWorker_Factory(reviewItemDaoProvider, imageClusterDaoProvider, clusteringHelperProvider, settingsRepositoryProvider);
  }

  public static ClusteringWorker newInstance(Context appContext, WorkerParameters workerParams,
      ReviewItemDao reviewItemDao, ImageClusterDao imageClusterDao,
      ImageClusteringHelper clusteringHelper, SettingsRepository settingsRepository) {
    return new ClusteringWorker(appContext, workerParams, reviewItemDao, imageClusterDao, clusteringHelper, settingsRepository);
  }
}
