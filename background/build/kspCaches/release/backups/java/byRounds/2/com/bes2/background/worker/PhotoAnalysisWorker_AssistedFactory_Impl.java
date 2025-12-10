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
public final class PhotoAnalysisWorker_AssistedFactory_Impl implements PhotoAnalysisWorker_AssistedFactory {
  private final PhotoAnalysisWorker_Factory delegateFactory;

  PhotoAnalysisWorker_AssistedFactory_Impl(PhotoAnalysisWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public PhotoAnalysisWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<PhotoAnalysisWorker_AssistedFactory> create(
      PhotoAnalysisWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PhotoAnalysisWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<PhotoAnalysisWorker_AssistedFactory> createFactoryProvider(
      PhotoAnalysisWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PhotoAnalysisWorker_AssistedFactory_Impl(delegateFactory));
  }
}
