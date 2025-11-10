package com.bes2.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;
import com.bes2.app.di.AnalyzerModule_ProvideNimaQualityAnalyzerFactory;
import com.bes2.app.ui.review.ReviewActivity;
import com.bes2.app.ui.review.ReviewViewModel;
import com.bes2.app.ui.review.ReviewViewModel_HiltModules;
import com.bes2.app.ui.settings.SettingsViewModel;
import com.bes2.app.ui.settings.SettingsViewModel_HiltModules;
import com.bes2.background.service.MediaDetectionService;
import com.bes2.background.service.MediaDetectionService_MembersInjector;
import com.bes2.background.worker.ClusteringWorker;
import com.bes2.background.worker.ClusteringWorker_AssistedFactory;
import com.bes2.background.worker.DailyCloudSyncWorker;
import com.bes2.background.worker.DailyCloudSyncWorker_AssistedFactory;
import com.bes2.background.worker.DeletionWorker;
import com.bes2.background.worker.DeletionWorker_AssistedFactory;
import com.bes2.background.worker.PhotoAnalysisWorker;
import com.bes2.background.worker.PhotoAnalysisWorker_AssistedFactory;
import com.bes2.core_ui.di.DefaultResourceProvider;
import com.bes2.data.dao.ImageClusterDao;
import com.bes2.data.dao.ImageItemDao;
import com.bes2.data.db.Bes2Database;
import com.bes2.data.di.DatabaseModule_ProvideBes2DatabaseFactory;
import com.bes2.data.di.DatabaseModule_ProvideImageClusterDaoFactory;
import com.bes2.data.di.DatabaseModule_ProvideImageItemDaoFactory;
import com.bes2.data.repository.SettingsRepository;
import com.bes2.ml.EyeClosedDetector;
import com.bes2.ml.FaceEmbedder;
import com.bes2.ml.NimaQualityAnalyzer;
import com.bes2.ml.SmileDetector;
import com.bes2.photos_integration.auth.GooglePhotosAuthManager;
import com.bes2.photos_integration.di.NetworkModule_ProvideGooglePhotosApiServiceFactory;
import com.bes2.photos_integration.di.NetworkModule_ProvideOkHttpClientFactory;
import com.bes2.photos_integration.di.NetworkModule_ProvideRetrofitFactory;
import com.bes2.photos_integration.google.GooglePhotosProvider;
import com.bes2.photos_integration.network.GooglePhotosApiService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerBes2Application_HiltComponents_SingletonC {
  private DaggerBes2Application_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public Bes2Application_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements Bes2Application_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements Bes2Application_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements Bes2Application_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements Bes2Application_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements Bes2Application_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements Bes2Application_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements Bes2Application_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public Bes2Application_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends Bes2Application_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends Bes2Application_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends Bes2Application_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends Bes2Application_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public void injectReviewActivity(ReviewActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>of(LazyClassKeyProvider.com_bes2_app_ui_review_ReviewViewModel, ReviewViewModel_HiltModules.KeyModule.provide(), LazyClassKeyProvider.com_bes2_app_ui_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()));
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_bes2_app_ui_settings_SettingsViewModel = "com.bes2.app.ui.settings.SettingsViewModel";

      static String com_bes2_app_ui_review_ReviewViewModel = "com.bes2.app.ui.review.ReviewViewModel";

      @KeepFieldType
      SettingsViewModel com_bes2_app_ui_settings_SettingsViewModel2;

      @KeepFieldType
      ReviewViewModel com_bes2_app_ui_review_ReviewViewModel2;
    }
  }

  private static final class ViewModelCImpl extends Bes2Application_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<ReviewViewModel> reviewViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.reviewViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>of(LazyClassKeyProvider.com_bes2_app_ui_review_ReviewViewModel, ((Provider) reviewViewModelProvider), LazyClassKeyProvider.com_bes2_app_ui_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)));
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_bes2_app_ui_settings_SettingsViewModel = "com.bes2.app.ui.settings.SettingsViewModel";

      static String com_bes2_app_ui_review_ReviewViewModel = "com.bes2.app.ui.review.ReviewViewModel";

      @KeepFieldType
      SettingsViewModel com_bes2_app_ui_settings_SettingsViewModel2;

      @KeepFieldType
      ReviewViewModel com_bes2_app_ui_review_ReviewViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.bes2.app.ui.review.ReviewViewModel 
          return (T) new ReviewViewModel(singletonCImpl.provideImageClusterDaoProvider.get(), singletonCImpl.provideImageItemDaoProvider.get());

          case 1: // com.bes2.app.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.settingsRepositoryProvider.get(), singletonCImpl.googlePhotosAuthManagerProvider.get(), singletonCImpl.provideWorkManagerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends Bes2Application_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends Bes2Application_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMediaDetectionService(MediaDetectionService arg0) {
      injectMediaDetectionService2(arg0);
    }

    @CanIgnoreReturnValue
    private MediaDetectionService injectMediaDetectionService2(MediaDetectionService instance) {
      MediaDetectionService_MembersInjector.injectImageDao(instance, singletonCImpl.provideImageItemDaoProvider.get());
      MediaDetectionService_MembersInjector.injectWorkManager(instance, singletonCImpl.provideWorkManagerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends Bes2Application_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<Bes2Database> provideBes2DatabaseProvider;

    private Provider<ImageItemDao> provideImageItemDaoProvider;

    private Provider<ClusteringWorker_AssistedFactory> clusteringWorker_AssistedFactoryProvider;

    private Provider<GooglePhotosAuthManager> googlePhotosAuthManagerProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<GooglePhotosApiService> provideGooglePhotosApiServiceProvider;

    private Provider<GooglePhotosProvider> googlePhotosProvider;

    private Provider<DailyCloudSyncWorker_AssistedFactory> dailyCloudSyncWorker_AssistedFactoryProvider;

    private Provider<DeletionWorker_AssistedFactory> deletionWorker_AssistedFactoryProvider;

    private Provider<WorkManager> provideWorkManagerProvider;

    private Provider<NimaQualityAnalyzer> provideNimaQualityAnalyzerProvider;

    private Provider<PhotoAnalysisWorker_AssistedFactory> photoAnalysisWorker_AssistedFactoryProvider;

    private Provider<ImageClusterDao> provideImageClusterDaoProvider;

    private Provider<SettingsRepository> settingsRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private EyeClosedDetector eyeClosedDetector() {
      return new EyeClosedDetector(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private FaceEmbedder faceEmbedder() {
      return new FaceEmbedder(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return ImmutableMap.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>of("com.bes2.background.worker.ClusteringWorker", ((Provider) clusteringWorker_AssistedFactoryProvider), "com.bes2.background.worker.DailyCloudSyncWorker", ((Provider) dailyCloudSyncWorker_AssistedFactoryProvider), "com.bes2.background.worker.DeletionWorker", ((Provider) deletionWorker_AssistedFactoryProvider), "com.bes2.background.worker.PhotoAnalysisWorker", ((Provider) photoAnalysisWorker_AssistedFactoryProvider));
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideBes2DatabaseProvider = DoubleCheck.provider(new SwitchingProvider<Bes2Database>(singletonCImpl, 2));
      this.provideImageItemDaoProvider = DoubleCheck.provider(new SwitchingProvider<ImageItemDao>(singletonCImpl, 1));
      this.clusteringWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<ClusteringWorker_AssistedFactory>(singletonCImpl, 0));
      this.googlePhotosAuthManagerProvider = DoubleCheck.provider(new SwitchingProvider<GooglePhotosAuthManager>(singletonCImpl, 5));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 8));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 7));
      this.provideGooglePhotosApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<GooglePhotosApiService>(singletonCImpl, 6));
      this.googlePhotosProvider = DoubleCheck.provider(new SwitchingProvider<GooglePhotosProvider>(singletonCImpl, 4));
      this.dailyCloudSyncWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<DailyCloudSyncWorker_AssistedFactory>(singletonCImpl, 3));
      this.deletionWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<DeletionWorker_AssistedFactory>(singletonCImpl, 9));
      this.provideWorkManagerProvider = DoubleCheck.provider(new SwitchingProvider<WorkManager>(singletonCImpl, 11));
      this.provideNimaQualityAnalyzerProvider = DoubleCheck.provider(new SwitchingProvider<NimaQualityAnalyzer>(singletonCImpl, 12));
      this.photoAnalysisWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<PhotoAnalysisWorker_AssistedFactory>(singletonCImpl, 10));
      this.provideImageClusterDaoProvider = DoubleCheck.provider(new SwitchingProvider<ImageClusterDao>(singletonCImpl, 13));
      this.settingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 14));
    }

    @Override
    public void injectBes2Application(Bes2Application bes2Application) {
      injectBes2Application2(bes2Application);
    }

    @Override
    public HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @Override
    public HiltWorkerFactory workerFactory() {
      return hiltWorkerFactory();
    }

    @Override
    public void inject(HiltWorkManagerInitializer initializer) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private Bes2Application injectBes2Application2(Bes2Application instance) {
      Bes2Application_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.bes2.background.worker.ClusteringWorker_AssistedFactory 
          return (T) new ClusteringWorker_AssistedFactory() {
            @Override
            public ClusteringWorker create(Context appContext, WorkerParameters workerParams) {
              return new ClusteringWorker(appContext, workerParams, singletonCImpl.provideImageItemDaoProvider.get(), new DefaultResourceProvider());
            }
          };

          case 1: // com.bes2.data.dao.ImageItemDao 
          return (T) DatabaseModule_ProvideImageItemDaoFactory.provideImageItemDao(singletonCImpl.provideBes2DatabaseProvider.get());

          case 2: // com.bes2.data.db.Bes2Database 
          return (T) DatabaseModule_ProvideBes2DatabaseFactory.provideBes2Database(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.bes2.background.worker.DailyCloudSyncWorker_AssistedFactory 
          return (T) new DailyCloudSyncWorker_AssistedFactory() {
            @Override
            public DailyCloudSyncWorker create(Context appContext2,
                WorkerParameters workerParams2) {
              return new DailyCloudSyncWorker(appContext2, workerParams2, singletonCImpl.provideImageItemDaoProvider.get(), singletonCImpl.googlePhotosProvider.get());
            }
          };

          case 4: // com.bes2.photos_integration.google.GooglePhotosProvider 
          return (T) new GooglePhotosProvider(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.googlePhotosAuthManagerProvider.get(), singletonCImpl.provideGooglePhotosApiServiceProvider.get());

          case 5: // com.bes2.photos_integration.auth.GooglePhotosAuthManager 
          return (T) new GooglePhotosAuthManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.bes2.photos_integration.network.GooglePhotosApiService 
          return (T) NetworkModule_ProvideGooglePhotosApiServiceFactory.provideGooglePhotosApiService(singletonCImpl.provideRetrofitProvider.get());

          case 7: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 8: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 9: // com.bes2.background.worker.DeletionWorker_AssistedFactory 
          return (T) new DeletionWorker_AssistedFactory() {
            @Override
            public DeletionWorker create(Context context, WorkerParameters workerParams3) {
              return new DeletionWorker(context, workerParams3, singletonCImpl.provideImageItemDaoProvider.get());
            }
          };

          case 10: // com.bes2.background.worker.PhotoAnalysisWorker_AssistedFactory 
          return (T) new PhotoAnalysisWorker_AssistedFactory() {
            @Override
            public PhotoAnalysisWorker create(Context appContext3, WorkerParameters workerParams4) {
              return new PhotoAnalysisWorker(appContext3, workerParams4, singletonCImpl.provideImageItemDaoProvider.get(), singletonCImpl.provideWorkManagerProvider.get(), singletonCImpl.provideNimaQualityAnalyzerProvider.get(), singletonCImpl.eyeClosedDetector(), singletonCImpl.faceEmbedder(), new SmileDetector(), new DefaultResourceProvider());
            }
          };

          case 11: // androidx.work.WorkManager 
          return (T) AppModule_ProvideWorkManagerFactory.provideWorkManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.bes2.ml.NimaQualityAnalyzer 
          return (T) AnalyzerModule_ProvideNimaQualityAnalyzerFactory.provideNimaQualityAnalyzer(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.bes2.data.dao.ImageClusterDao 
          return (T) DatabaseModule_ProvideImageClusterDaoFactory.provideImageClusterDao(singletonCImpl.provideBes2DatabaseProvider.get());

          case 14: // com.bes2.data.repository.SettingsRepository 
          return (T) new SettingsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
