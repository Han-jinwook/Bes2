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
public final class DailyCloudSyncWorker_AssistedFactory_Impl implements DailyCloudSyncWorker_AssistedFactory {
  private final DailyCloudSyncWorker_Factory delegateFactory;

  DailyCloudSyncWorker_AssistedFactory_Impl(DailyCloudSyncWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public DailyCloudSyncWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<DailyCloudSyncWorker_AssistedFactory> create(
      DailyCloudSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DailyCloudSyncWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<DailyCloudSyncWorker_AssistedFactory> createFactoryProvider(
      DailyCloudSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new DailyCloudSyncWorker_AssistedFactory_Impl(delegateFactory));
  }
}
