package com.bes2.data.repository;

import com.bes2.data.dao.ImageItemDao;
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
public final class HomeRepository_Factory implements Factory<HomeRepository> {
  private final Provider<ImageItemDao> imageItemDaoProvider;

  public HomeRepository_Factory(Provider<ImageItemDao> imageItemDaoProvider) {
    this.imageItemDaoProvider = imageItemDaoProvider;
  }

  @Override
  public HomeRepository get() {
    return newInstance(imageItemDaoProvider.get());
  }

  public static HomeRepository_Factory create(Provider<ImageItemDao> imageItemDaoProvider) {
    return new HomeRepository_Factory(imageItemDaoProvider);
  }

  public static HomeRepository newInstance(ImageItemDao imageItemDao) {
    return new HomeRepository(imageItemDao);
  }
}
