package com.bes2.background.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.bes2.data.dao.ReviewItemDao;
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
  private final Provider<ReviewItemDao> reviewItemDaoProvider;

  public DeletionWorker_Factory(Provider<ReviewItemDao> reviewItemDaoProvider) {
    this.reviewItemDaoProvider = reviewItemDaoProvider;
  }

  public DeletionWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, reviewItemDaoProvider.get());
  }

  public static DeletionWorker_Factory create(Provider<ReviewItemDao> reviewItemDaoProvider) {
    return new DeletionWorker_Factory(reviewItemDaoProvider);
  }

  public static DeletionWorker newInstance(Context appContext, WorkerParameters workerParams,
      ReviewItemDao reviewItemDao) {
    return new DeletionWorker(appContext, workerParams, reviewItemDao);
  }
}
