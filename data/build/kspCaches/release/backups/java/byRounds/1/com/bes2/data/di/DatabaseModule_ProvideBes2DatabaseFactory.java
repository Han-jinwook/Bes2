package com.bes2.data.di;

import android.content.Context;
import com.bes2.data.db.Bes2Database;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideBes2DatabaseFactory implements Factory<Bes2Database> {
  private final Provider<Context> appContextProvider;

  public DatabaseModule_ProvideBes2DatabaseFactory(Provider<Context> appContextProvider) {
    this.appContextProvider = appContextProvider;
  }

  @Override
  public Bes2Database get() {
    return provideBes2Database(appContextProvider.get());
  }

  public static DatabaseModule_ProvideBes2DatabaseFactory create(
      Provider<Context> appContextProvider) {
    return new DatabaseModule_ProvideBes2DatabaseFactory(appContextProvider);
  }

  public static Bes2Database provideBes2Database(Context appContext) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBes2Database(appContext));
  }
}
