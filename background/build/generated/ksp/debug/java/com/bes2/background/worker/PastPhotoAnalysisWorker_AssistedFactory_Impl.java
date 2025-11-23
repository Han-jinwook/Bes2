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
public final class PastPhotoAnalysisWorker_AssistedFactory_Impl implements PastPhotoAnalysisWorker_AssistedFactory {
  private final PastPhotoAnalysisWorker_Factory delegateFactory;

  PastPhotoAnalysisWorker_AssistedFactory_Impl(PastPhotoAnalysisWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public PastPhotoAnalysisWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<PastPhotoAnalysisWorker_AssistedFactory> create(
      PastPhotoAnalysisWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PastPhotoAnalysisWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<PastPhotoAnalysisWorker_AssistedFactory> createFactoryProvider(
      PastPhotoAnalysisWorker_Factory delegateFactory) {
    return InstanceFactory.create(new PastPhotoAnalysisWorker_AssistedFactory_Impl(delegateFactory));
  }
}
