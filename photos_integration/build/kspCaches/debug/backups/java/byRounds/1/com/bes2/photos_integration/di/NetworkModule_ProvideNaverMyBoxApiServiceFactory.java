package com.bes2.photos_integration.di;

import com.bes2.photos_integration.network.NaverMyBoxApiService;
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
public final class NetworkModule_ProvideNaverMyBoxApiServiceFactory implements Factory<NaverMyBoxApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideNaverMyBoxApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public NaverMyBoxApiService get() {
    return provideNaverMyBoxApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideNaverMyBoxApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideNaverMyBoxApiServiceFactory(retrofitProvider);
  }

  public static NaverMyBoxApiService provideNaverMyBoxApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideNaverMyBoxApiService(retrofit));
  }
}
