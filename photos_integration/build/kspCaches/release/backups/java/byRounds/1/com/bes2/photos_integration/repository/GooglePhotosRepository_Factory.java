package com.bes2.photos_integration.repository;

import com.bes2.photos_integration.auth.GooglePhotosAuthManager;
import com.bes2.photos_integration.network.GooglePhotosApiService;
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
public final class GooglePhotosRepository_Factory implements Factory<GooglePhotosRepository> {
  private final Provider<GooglePhotosAuthManager> authManagerProvider;

  private final Provider<GooglePhotosApiService> apiServiceProvider;

  public GooglePhotosRepository_Factory(Provider<GooglePhotosAuthManager> authManagerProvider,
      Provider<GooglePhotosApiService> apiServiceProvider) {
    this.authManagerProvider = authManagerProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public GooglePhotosRepository get() {
    return newInstance(authManagerProvider.get(), apiServiceProvider.get());
  }

  public static GooglePhotosRepository_Factory create(
      Provider<GooglePhotosAuthManager> authManagerProvider,
      Provider<GooglePhotosApiService> apiServiceProvider) {
    return new GooglePhotosRepository_Factory(authManagerProvider, apiServiceProvider);
  }

  public static GooglePhotosRepository newInstance(GooglePhotosAuthManager authManager,
      GooglePhotosApiService apiService) {
    return new GooglePhotosRepository(authManager, apiService);
  }
}
