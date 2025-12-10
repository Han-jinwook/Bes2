package com.bes2.ml;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class FaceRestorationProcessor_Factory implements Factory<FaceRestorationProcessor> {
  private final Provider<Context> contextProvider;

  public FaceRestorationProcessor_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FaceRestorationProcessor get() {
    return newInstance(contextProvider.get());
  }

  public static FaceRestorationProcessor_Factory create(Provider<Context> contextProvider) {
    return new FaceRestorationProcessor_Factory(contextProvider);
  }

  public static FaceRestorationProcessor newInstance(Context context) {
    return new FaceRestorationProcessor(context);
  }
}
