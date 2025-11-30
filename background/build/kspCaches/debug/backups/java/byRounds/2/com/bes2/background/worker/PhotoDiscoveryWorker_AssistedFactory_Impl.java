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
public final class PhotoDiscoveryWorker_AssistedFactory_Impl implements PhotoDiscoveryWorker_AssistedFactory {
  private final PhotoDiscoveryWorker_Factory delegateFactory;

  PhotoDiscoveryWorker_AssistedFactory_Impl(PhotoDiscoveryWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public PhotoDiscoveryWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<PhotoDiscoveryWorker_AssistedFactory> create(
      PhotoDiscoveryWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PhotoDiscoveryWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<PhotoDiscoveryWorker_AssistedFactory> createFactoryProvider(
      PhotoDiscoveryWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PhotoDiscoveryWorker_AssistedFactory_Impl(delegateFactory));
  }
}
