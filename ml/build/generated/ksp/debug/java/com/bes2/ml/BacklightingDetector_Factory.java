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
public final class BacklightingDetector_Factory implements Factory<BacklightingDetector> {
  @Override
  public BacklightingDetector get() {
    return newInstance();
  }

  public static BacklightingDetector_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BacklightingDetector newInstance() {
    return new BacklightingDetector();
  }

  private static final class InstanceHolder {
    private static final BacklightingDetector_Factory INSTANCE = new BacklightingDetector_Factory();
  }
}
