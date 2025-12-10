package com.bes2.photos_integration.google;

import android.content.Context;
import com.bes2.photos_integration.auth.GooglePhotosAuthManager;
import com.bes2.photos_integration.network.GooglePhotosApiService;
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
public final class GooglePhotosProvider_Factory implements Factory<GooglePhotosProvider> {
  private final Provider<Context> contextProvider;

  private final Provider<GooglePhotosAuthManager> authManagerProvider;

  private final Provider<GooglePhotosApiService> apiServiceProvider;

  public GooglePhotosProvider_Factory(Provider<Context> contextProvider,
      Provider<GooglePhotosAuthManager> authManagerProvider,
      Provider<GooglePhotosApiService> apiServiceProvider) {
    this.contextProvider = contextProvider;
    this.authManagerProvider = authManagerProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public GooglePhotosProvider get() {
    return newInstance(contextProvider.get(), authManagerProvider.get(), apiServiceProvider.get());
  }

  public static GooglePhotosProvider_Factory create(Provider<Context> contextProvider,
      Provider<GooglePhotosAuthManager> authManagerProvider,
      Provider<GooglePhotosApiService> apiServiceProvider) {
    return new GooglePhotosProvider_Factory(contextProvider, authManagerProvider, apiServiceProvider);
  }

  public static GooglePhotosProvider newInstance(Context context,
      GooglePhotosAuthManager authManager, GooglePhotosApiService apiService) {
    return new GooglePhotosProvider(context, authManager, apiService);
  }
}
