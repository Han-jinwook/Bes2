package com.bes2.photos_integration.naver;

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
public final class NaverMyBoxProvider_Factory implements Factory<NaverMyBoxProvider> {
  @Override
  public NaverMyBoxProvider get() {
    return newInstance();
  }

  public static NaverMyBoxProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NaverMyBoxProvider newInstance() {
    return new NaverMyBoxProvider();
  }

  private static final class InstanceHolder {
    private static final NaverMyBoxProvider_Factory INSTANCE = new NaverMyBoxProvider_Factory();
  }
}
