package com.bes2.photos_integration.auth;

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
public final class NaverMyBoxAuthManager_Factory implements Factory<NaverMyBoxAuthManager> {
  private final Provider<Context> contextProvider;

  private final Provider<NaverAuthInfo> naverAuthInfoProvider;

  public NaverMyBoxAuthManager_Factory(Provider<Context> contextProvider,
      Provider<NaverAuthInfo> naverAuthInfoProvider) {
    this.contextProvider = contextProvider;
    this.naverAuthInfoProvider = naverAuthInfoProvider;
  }

  @Override
  public NaverMyBoxAuthManager get() {
    return newInstance(contextProvider.get(), naverAuthInfoProvider.get());
  }

  public static NaverMyBoxAuthManager_Factory create(Provider<Context> contextProvider,
      Provider<NaverAuthInfo> naverAuthInfoProvider) {
    return new NaverMyBoxAuthManager_Factory(contextProvider, naverAuthInfoProvider);
  }

  public static NaverMyBoxAuthManager newInstance(Context context, NaverAuthInfo naverAuthInfo) {
    return new NaverMyBoxAuthManager(context, naverAuthInfo);
  }
}
