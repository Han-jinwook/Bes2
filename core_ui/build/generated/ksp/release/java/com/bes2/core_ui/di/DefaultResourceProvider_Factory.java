package com.bes2.core_ui.di;

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
public final class DefaultResourceProvider_Factory implements Factory<DefaultResourceProvider> {
  @Override
  public DefaultResourceProvider get() {
    return newInstance();
  }

  public static DefaultResourceProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DefaultResourceProvider newInstance() {
    return new DefaultResourceProvider();
  }

  private static final class InstanceHolder {
    private static final DefaultResourceProvider_Factory INSTANCE = new DefaultResourceProvider_Factory();
  }
}
