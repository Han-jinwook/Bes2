package com.bes2.data.di;

import com.bes2.data.dao.TrashItemDao;
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
public final class DatabaseModule_ProvideTrashItemDaoFactory implements Factory<TrashItemDao> {
  private final Provider<Bes2Database> databaseProvider;

  public DatabaseModule_ProvideTrashItemDaoFactory(Provider<Bes2Database> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TrashItemDao get() {
    return provideTrashItemDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideTrashItemDaoFactory create(
      Provider<Bes2Database> databaseProvider) {
    return new DatabaseModule_ProvideTrashItemDaoFactory(databaseProvider);
  }

  public static TrashItemDao provideTrashItemDao(Bes2Database database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTrashItemDao(database));
  }
}
