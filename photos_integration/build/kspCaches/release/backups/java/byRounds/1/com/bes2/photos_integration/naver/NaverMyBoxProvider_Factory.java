package com.bes2.photos_integration.naver;

import android.content.Context;
import com.bes2.photos_integration.auth.NaverMyBoxAuthManager;
import com.bes2.photos_integration.network.NaverMyBoxApiService;
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
public final class NaverMyBoxProvider_Factory implements Factory<NaverMyBoxProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<NaverMyBoxAuthManager> authManagerProvider;

  private final Provider<NaverMyBoxApiService> apiServiceProvider;

  public NaverMyBoxProvider_Factory(Provider<Context> contextProvider,
      Provider<NaverMyBoxAuthManager> authManagerProvider,
      Provider<NaverMyBoxApiService> apiServiceProvider) {
    this.contextProvider = contextProvider;
    this.authManagerProvider = authManagerProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public NaverMyBoxProvider get() {
    return newInstance(contextProvider.get(), authManagerProvider.get(), apiServiceProvider.get());
  }

  public static NaverMyBoxProvider_Factory create(Provider<Context> contextProvider,
      Provider<NaverMyBoxAuthManager> authManagerProvider,
      Provider<NaverMyBoxApiService> apiServiceProvider) {
    return new NaverMyBoxProvider_Factory(contextProvider, authManagerProvider, apiServiceProvider);
  }

  public static NaverMyBoxProvider newInstance(Context context, NaverMyBoxAuthManager authManager,
      NaverMyBoxApiService apiService) {
    return new NaverMyBoxProvider(context, authManager, apiService);
  }
}
