package com.bes2.ml;

import android.content.Context;
import com.bes2.ml.util.SimpleTokenizer;
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
public final class SemanticSearchEngine_Factory implements Factory<SemanticSearchEngine> {
  private final Provider<Context> contextProvider;

  private final Provider<SimpleTokenizer> tokenizerProvider;

  public SemanticSearchEngine_Factory(Provider<Context> contextProvider,
      Provider<SimpleTokenizer> tokenizerProvider) {
    this.contextProvider = contextProvider;
    this.tokenizerProvider = tokenizerProvider;
  }

  @Override
  public SemanticSearchEngine get() {
    return newInstance(contextProvider.get(), tokenizerProvider.get());
  }

  public static SemanticSearchEngine_Factory create(Provider<Context> contextProvider,
      Provider<SimpleTokenizer> tokenizerProvider) {
    return new SemanticSearchEngine_Factory(contextProvider, tokenizerProvider);
  }

  public static SemanticSearchEngine newInstance(Context context, SimpleTokenizer tokenizer) {
    return new SemanticSearchEngine(context, tokenizer);
  }
}
