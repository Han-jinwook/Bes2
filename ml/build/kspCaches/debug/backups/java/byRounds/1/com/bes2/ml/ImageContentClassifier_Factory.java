package com.bes2.ml;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ImageContentClassifier_Factory implements Factory<ImageContentClassifier> {
  @Override
  public ImageContentClassifier get() {
    return newInstance();
  }

  public static ImageContentClassifier_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImageContentClassifier newInstance() {
    return new ImageContentClassifier();
  }

  private static final class InstanceHolder {
    private static final ImageContentClassifier_Factory INSTANCE = new ImageContentClassifier_Factory();
  }
}
