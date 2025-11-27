package com.bes2.ml.util;

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
public final class SimpleTokenizer_Factory implements Factory<SimpleTokenizer> {
  private final Provider<Context> contextProvider;

  public SimpleTokenizer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SimpleTokenizer get() {
    return newInstance(contextProvider.get());
  }

  public static SimpleTokenizer_Factory create(Provider<Context> contextProvider) {
    return new SimpleTokenizer_Factory(contextProvider);
  }

  public static SimpleTokenizer newInstance(Context context) {
    return new SimpleTokenizer(context);
  }
}
