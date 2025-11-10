package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.core_common.provider.ResourceProvider;
import com.bes2.data.dao.ImageItemDao;
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

  private final Provider<ResourceProvider> resourceProvider;

  public ClusteringWorker_Factory(Provider<ImageItemDao> imageDaoProvider,
      Provider<ResourceProvider> resourceProvider) {
    this.imageDaoProvider = imageDaoProvider;
    this.resourceProvider = resourceProvider;
  }

  public ClusteringWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, imageDaoProvider.get(), resourceProvider.get());
  }

  public static ClusteringWorker_Factory create(Provider<ImageItemDao> imageDaoProvider,
      Provider<ResourceProvider> resourceProvider) {
    return new ClusteringWorker_Factory(imageDaoProvider, resourceProvider);
  }

  public static ClusteringWorker newInstance(Context appContext, WorkerParameters workerParams,
      ImageItemDao imageDao, ResourceProvider resourceProvider) {
    return new ClusteringWorker(appContext, workerParams, imageDao, resourceProvider);
  }
}
