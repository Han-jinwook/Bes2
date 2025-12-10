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
public final class MusiqQualityAnalyzer_Factory implements Factory<MusiqQualityAnalyzer> {
  private final Provider<Context> contextProvider;

  public MusiqQualityAnalyzer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MusiqQualityAnalyzer get() {
    return newInstance(contextProvider.get());
  }

  public static MusiqQualityAnalyzer_Factory create(Provider<Context> contextProvider) {
    return new MusiqQualityAnalyzer_Factory(contextProvider);
  }

  public static MusiqQualityAnalyzer newInstance(Context context) {
    return new MusiqQualityAnalyzer(context);
  }
}
