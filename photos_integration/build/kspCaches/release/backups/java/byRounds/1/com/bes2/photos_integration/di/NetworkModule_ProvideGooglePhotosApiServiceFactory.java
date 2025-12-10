package com.bes2.photos_integration.di;

import com.bes2.photos_integration.network.GooglePhotosApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideGooglePhotosApiServiceFactory implements Factory<GooglePhotosApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideGooglePhotosApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public GooglePhotosApiService get() {
    return provideGooglePhotosApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideGooglePhotosApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideGooglePhotosApiServiceFactory(retrofitProvider);
  }

  public static GooglePhotosApiService provideGooglePhotosApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideGooglePhotosApiService(retrofit));
  }
}
