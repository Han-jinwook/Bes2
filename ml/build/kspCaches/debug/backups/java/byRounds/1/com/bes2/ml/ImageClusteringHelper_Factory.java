package com.bes2.ml;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
  @Override
  public ImageClusteringHelper get() {
    return newInstance();
  }

  public static ImageClusteringHelper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImageClusteringHelper newInstance() {
    return new ImageClusteringHelper();
  }

  private static final class InstanceHolder {
    private static final ImageClusteringHelper_Factory INSTANCE = new ImageClusteringHelper_Factory();
  }
}
