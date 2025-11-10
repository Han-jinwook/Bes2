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
public final class FaceEmbedder_Factory implements Factory<FaceEmbedder> {
  private final Provider<Context> contextProvider;

  public FaceEmbedder_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FaceEmbedder get() {
    return newInstance(contextProvider.get());
  }

  public static FaceEmbedder_Factory create(Provider<Context> contextProvider) {
    return new FaceEmbedder_Factory(contextProvider);
  }

  public static FaceEmbedder newInstance(Context context) {
    return new FaceEmbedder(context);
  }
}
