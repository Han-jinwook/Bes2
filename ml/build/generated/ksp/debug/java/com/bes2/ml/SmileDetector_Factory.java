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
public final class SmileDetector_Factory implements Factory<SmileDetector> {
  @Override
  public SmileDetector get() {
    return newInstance();
  }

  public static SmileDetector_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SmileDetector newInstance() {
    return new SmileDetector();
  }

  private static final class InstanceHolder {
    private static final SmileDetector_Factory INSTANCE = new SmileDetector_Factory();
  }
}
