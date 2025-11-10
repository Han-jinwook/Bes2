package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ClusteringWorker_AssistedFactory_Impl implements ClusteringWorker_AssistedFactory {
  private final ClusteringWorker_Factory delegateFactory;

  ClusteringWorker_AssistedFactory_Impl(ClusteringWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ClusteringWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<ClusteringWorker_AssistedFactory> create(
      ClusteringWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ClusteringWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<ClusteringWorker_AssistedFactory> createFactoryProvider(
      ClusteringWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ClusteringWorker_AssistedFactory_Impl(delegateFactory));
  }
}
