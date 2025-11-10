package com.bes2.data.di;

import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.db.Bes2Database;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideImageItemDaoFactory implements Factory<ImageItemDao> {
  private final Provider<Bes2Database> databaseProvider;

  public DatabaseModule_ProvideImageItemDaoFactory(Provider<Bes2Database> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ImageItemDao get() {
    return provideImageItemDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideImageItemDaoFactory create(
      Provider<Bes2Database> databaseProvider) {
    return new DatabaseModule_ProvideImageItemDaoFactory(databaseProvider);
  }

  public static ImageItemDao provideImageItemDao(Bes2Database database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideImageItemDao(database));
  }
}
