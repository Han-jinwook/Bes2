package com.bes2.background.util;

import com.bes2.ml.FaceEmbedder;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ImageClusteringHelper_Factory implements Factory<ImageClusteringHelper> {
  private final Provider<FaceEmbedder> faceEmbedderProvider;

  public ImageClusteringHelper_Factory(Provider<FaceEmbedder> faceEmbedderProvider) {
    this.faceEmbedderProvider = faceEmbedderProvider;
  }

  @Override
  public ImageClusteringHelper get() {
    return newInstance(faceEmbedderProvider.get());
  }

  public static ImageClusteringHelper_Factory create(Provider<FaceEmbedder> faceEmbedderProvider) {
    return new ImageClusteringHelper_Factory(faceEmbedderProvider);
  }

  public static ImageClusteringHelper newInstance(FaceEmbedder faceEmbedder) {
    return new ImageClusteringHelper(faceEmbedder);
  }
}
