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
public final class GooglePhotosAuthManager_Factory implements Factory<GooglePhotosAuthManager> {
  private final Provider<Context> contextProvider;

  public GooglePhotosAuthManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GooglePhotosAuthManager get() {
    return newInstance(contextProvider.get());
  }

  public static GooglePhotosAuthManager_Factory create(Provider<Context> contextProvider) {
    return new GooglePhotosAuthManager_Factory(contextProvider);
  }

  public static GooglePhotosAuthManager newInstance(Context context) {
    return new GooglePhotosAuthManager(context);
  }
}
