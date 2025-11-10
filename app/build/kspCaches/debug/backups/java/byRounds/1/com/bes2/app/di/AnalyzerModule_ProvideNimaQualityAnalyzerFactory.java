package com.bes2.app.di;

import android.content.Context;
import com.bes2.ml.NimaQualityAnalyzer;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AnalyzerModule_ProvideNimaQualityAnalyzerFactory implements Factory<NimaQualityAnalyzer> {
  private final Provider<Context> contextProvider;

  public AnalyzerModule_ProvideNimaQualityAnalyzerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NimaQualityAnalyzer get() {
    return provideNimaQualityAnalyzer(contextProvider.get());
  }

  public static AnalyzerModule_ProvideNimaQualityAnalyzerFactory create(
      Provider<Context> contextProvider) {
    return new AnalyzerModule_ProvideNimaQualityAnalyzerFactory(contextProvider);
  }

  public static NimaQualityAnalyzer provideNimaQualityAnalyzer(Context context) {
    return Preconditions.checkNotNullFromProvides(AnalyzerModule.INSTANCE.provideNimaQualityAnalyzer(context));
  }
}
