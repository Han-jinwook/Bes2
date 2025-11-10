package com.bes2.ml;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class EyeClosedDetector_Factory implements Factory<EyeClosedDetector> {
  private final Provider<Context> contextProvider;

  public EyeClosedDetector_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public EyeClosedDetector get() {
    return newInstance(contextProvider.get());
  }

  public static EyeClosedDetector_Factory create(Provider<Context> contextProvider) {
    return new EyeClosedDetector_Factory(contextProvider);
  }

  public static EyeClosedDetector newInstance(Context context) {
    return new EyeClosedDetector(context);
  }
}
