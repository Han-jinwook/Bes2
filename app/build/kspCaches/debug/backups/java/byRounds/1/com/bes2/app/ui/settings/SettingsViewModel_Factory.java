package com.bes2.app.ui.settings;

import android.content.Context;
import androidx.work.WorkManager;
import com.bes2.data.repository.SettingsRepository;
import com.bes2.photos_integration.auth.GooglePhotosAuthManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<GooglePhotosAuthManager> authManagerProvider;

  private final Provider<WorkManager> workManagerProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GooglePhotosAuthManager> authManagerProvider,
      Provider<WorkManager> workManagerProvider, Provider<Context> contextProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.authManagerProvider = authManagerProvider;
    this.workManagerProvider = workManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), authManagerProvider.get(), workManagerProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GooglePhotosAuthManager> authManagerProvider,
      Provider<WorkManager> workManagerProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, authManagerProvider, workManagerProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      GooglePhotosAuthManager authManager, WorkManager workManager, Context context) {
    return new SettingsViewModel(settingsRepository, authManager, workManager, context);
  }
}
