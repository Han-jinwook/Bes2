package com.bes2.background.service;

import androidx.work.WorkManager;
import com.bes2.data.dao.ImageItemDao;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MediaDetectionService_MembersInjector implements MembersInjector<MediaDetectionService> {
  private final Provider<ImageItemDao> imageDaoProvider;

  private final Provider<WorkManager> workManagerProvider;

  public MediaDetectionService_MembersInjector(Provider<ImageItemDao> imageDaoProvider,
      Provider<WorkManager> workManagerProvider) {
    this.imageDaoProvider = imageDaoProvider;
    this.workManagerProvider = workManagerProvider;
  }

  public static MembersInjector<MediaDetectionService> create(
      Provider<ImageItemDao> imageDaoProvider, Provider<WorkManager> workManagerProvider) {
    return new MediaDetectionService_MembersInjector(imageDaoProvider, workManagerProvider);
  }

  @Override
  public void injectMembers(MediaDetectionService instance) {
    injectImageDao(instance, imageDaoProvider.get());
    injectWorkManager(instance, workManagerProvider.get());
  }

  @InjectedFieldSignature("com.bes2.background.service.MediaDetectionService.imageDao")
  public static void injectImageDao(MediaDetectionService instance, ImageItemDao imageDao) {
    instance.imageDao = imageDao;
  }

  @InjectedFieldSignature("com.bes2.background.service.MediaDetectionService.workManager")
  public static void injectWorkManager(MediaDetectionService instance, WorkManager workManager) {
    instance.workManager = workManager;
  }
}
