package com.bes2.background.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = PhotoAnalysisWorker.class
)
public interface PhotoAnalysisWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.bes2.background.worker.PhotoAnalysisWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      PhotoAnalysisWorker_AssistedFactory factory);
}
