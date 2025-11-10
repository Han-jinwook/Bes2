package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ImageItemDao;
import dagger.internal.DaggerGenerated;
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
public final class DeletionWorker_Factory {
  private final Provider<ImageItemDao> imageItemDaoProvider;

  public DeletionWorker_Factory(Provider<ImageItemDao> imageItemDaoProvider) {
    this.imageItemDaoProvider = imageItemDaoProvider;
  }

  public DeletionWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, imageItemDaoProvider.get());
  }

  public static DeletionWorker_Factory create(Provider<ImageItemDao> imageItemDaoProvider) {
    return new DeletionWorker_Factory(imageItemDaoProvider);
  }

  public static DeletionWorker newInstance(Context context, WorkerParameters workerParams,
      ImageItemDao imageItemDao) {
    return new DeletionWorker(context, workerParams, imageItemDao);
  }
}
