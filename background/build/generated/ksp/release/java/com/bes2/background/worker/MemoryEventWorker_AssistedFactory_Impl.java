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
public final class MemoryEventWorker_AssistedFactory_Impl implements MemoryEventWorker_AssistedFactory {
  private final MemoryEventWorker_Factory delegateFactory;

  MemoryEventWorker_AssistedFactory_Impl(MemoryEventWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public MemoryEventWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<MemoryEventWorker_AssistedFactory> create(
      MemoryEventWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MemoryEventWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<MemoryEventWorker_AssistedFactory> createFactoryProvider(
      MemoryEventWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MemoryEventWorker_AssistedFactory_Impl(delegateFactory));
  }
}
