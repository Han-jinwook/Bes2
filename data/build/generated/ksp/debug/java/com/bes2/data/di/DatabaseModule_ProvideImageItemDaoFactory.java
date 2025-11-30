package com.bes2.data.di;

import com.bes2.data.dao.ImageItemDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
  @Override
  public ImageItemDao get() {
    return provideImageItemDao();
  }

  public static DatabaseModule_ProvideImageItemDaoFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImageItemDao provideImageItemDao() {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideImageItemDao());
  }

  private static final class InstanceHolder {
    private static final DatabaseModule_ProvideImageItemDaoFactory INSTANCE = new DatabaseModule_ProvideImageItemDaoFactory();
  }
}
