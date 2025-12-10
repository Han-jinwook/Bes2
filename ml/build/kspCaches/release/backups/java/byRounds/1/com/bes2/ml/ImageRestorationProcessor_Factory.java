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
public final class ImageRestorationProcessor_Factory implements Factory<ImageRestorationProcessor> {
  private final Provider<Context> contextProvider;

  private final Provider<FaceRestorationProcessor> faceRestorationProcessorProvider;

  public ImageRestorationProcessor_Factory(Provider<Context> contextProvider,
      Provider<FaceRestorationProcessor> faceRestorationProcessorProvider) {
    this.contextProvider = contextProvider;
    this.faceRestorationProcessorProvider = faceRestorationProcessorProvider;
  }

  @Override
  public ImageRestorationProcessor get() {
    return newInstance(contextProvider.get(), faceRestorationProcessorProvider.get());
  }

  public static ImageRestorationProcessor_Factory create(Provider<Context> contextProvider,
      Provider<FaceRestorationProcessor> faceRestorationProcessorProvider) {
    return new ImageRestorationProcessor_Factory(contextProvider, faceRestorationProcessorProvider);
  }

  public static ImageRestorationProcessor newInstance(Context context,
      FaceRestorationProcessor faceRestorationProcessor) {
    return new ImageRestorationProcessor(context, faceRestorationProcessor);
  }
}
