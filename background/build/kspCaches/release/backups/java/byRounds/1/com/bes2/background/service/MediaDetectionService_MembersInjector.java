package com.bes2.background.service;

import androidx.work.WorkManager;
import com.bes2.data.dao.ReviewItemDao;
import com.bes2.data.dao.TrashItemDao;
import com.bes2.data.repository.SettingsRepository;
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
  private final Provider<ReviewItemDao> reviewItemDaoProvider;

  private final Provider<TrashItemDao> trashItemDaoProvider;

  private final Provider<WorkManager> workManagerProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public MediaDetectionService_MembersInjector(Provider<ReviewItemDao> reviewItemDaoProvider,
      Provider<TrashItemDao> trashItemDaoProvider, Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.reviewItemDaoProvider = reviewItemDaoProvider;
    this.trashItemDaoProvider = trashItemDaoProvider;
    this.workManagerProvider = workManagerProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public static MembersInjector<MediaDetectionService> create(
      Provider<ReviewItemDao> reviewItemDaoProvider, Provider<TrashItemDao> trashItemDaoProvider,
      Provider<WorkManager> workManagerProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new MediaDetectionService_MembersInjector(reviewItemDaoProvider, trashItemDaoProvider, workManagerProvider, settingsRepositoryProvider);
  }

  @Override
  public void injectMembers(MediaDetectionService instance) {
    injectReviewItemDao(instance, reviewItemDaoProvider.get());
    injectTrashItemDao(instance, trashItemDaoProvider.get());
    injectWorkManager(instance, workManagerProvider.get());
    injectSettingsRepository(instance, settingsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.bes2.background.service.MediaDetectionService.reviewItemDao")
  public static void injectReviewItemDao(MediaDetectionService instance,
      ReviewItemDao reviewItemDao) {
    instance.reviewItemDao = reviewItemDao;
  }

  @InjectedFieldSignature("com.bes2.background.service.MediaDetectionService.trashItemDao")
  public static void injectTrashItemDao(MediaDetectionService instance, TrashItemDao trashItemDao) {
    instance.trashItemDao = trashItemDao;
  }

  @InjectedFieldSignature("com.bes2.background.service.MediaDetectionService.workManager")
  public static void injectWorkManager(MediaDetectionService instance, WorkManager workManager) {
    instance.workManager = workManager;
  }

  @InjectedFieldSignature("com.bes2.background.service.MediaDetectionService.settingsRepository")
  public static void injectSettingsRepository(MediaDetectionService instance,
      SettingsRepository settingsRepository) {
    instance.settingsRepository = settingsRepository;
  }
}
