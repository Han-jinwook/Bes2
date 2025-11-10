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
public final class DeletionWorker_AssistedFactory_Impl implements DeletionWorker_AssistedFactory {
  private final DeletionWorker_Factory delegateFactory;

  DeletionWorker_AssistedFactory_Impl(DeletionWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public DeletionWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<DeletionWorker_AssistedFactory> create(
      DeletionWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DeletionWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<DeletionWorker_AssistedFactory> createFactoryProvider(
      DeletionWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DeletionWorker_AssistedFactory_Impl(delegateFactory));
  }
}
