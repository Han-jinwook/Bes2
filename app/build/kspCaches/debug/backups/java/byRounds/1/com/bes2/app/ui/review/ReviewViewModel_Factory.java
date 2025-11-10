package com.bes2.app.ui.review;

import com.bes2.data.dao.ImageClusterDao;
import com.bes2.data.dao.ImageItemDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ReviewViewModel_Factory implements Factory<ReviewViewModel> {
  private final Provider<ImageClusterDao> imageClusterDaoProvider;

  private final Provider<ImageItemDao> imageItemDaoProvider;

  public ReviewViewModel_Factory(Provider<ImageClusterDao> imageClusterDaoProvider,
      Provider<ImageItemDao> imageItemDaoProvider) {
    this.imageClusterDaoProvider = imageClusterDaoProvider;
    this.imageItemDaoProvider = imageItemDaoProvider;
  }

  @Override
  public ReviewViewModel get() {
    return newInstance(imageClusterDaoProvider.get(), imageItemDaoProvider.get());
  }

  public static ReviewViewModel_Factory create(Provider<ImageClusterDao> imageClusterDaoProvider,
      Provider<ImageItemDao> imageItemDaoProvider) {
    return new ReviewViewModel_Factory(imageClusterDaoProvider, imageItemDaoProvider);
  }

  public static ReviewViewModel newInstance(ImageClusterDao imageClusterDao,
      ImageItemDao imageItemDao) {
    return new ReviewViewModel(imageClusterDao, imageItemDao);
  }
}
