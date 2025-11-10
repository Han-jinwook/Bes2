package com.bes2.app;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class Bes2Application_MembersInjector implements MembersInjector<Bes2Application> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public Bes2Application_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<Bes2Application> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new Bes2Application_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(Bes2Application instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.bes2.app.Bes2Application.workerFactory")
  public static void injectWorkerFactory(Bes2Application instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
