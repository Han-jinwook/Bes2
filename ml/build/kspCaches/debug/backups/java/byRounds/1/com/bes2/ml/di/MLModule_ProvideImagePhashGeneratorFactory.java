package com.bes2.ml.di;

import com.bes2.ml.ImagePhashGenerator;
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
public final class MLModule_ProvideImagePhashGeneratorFactory implements Factory<ImagePhashGenerator> {
  @Override
  public ImagePhashGenerator get() {
    return provideImagePhashGenerator();
  }

  public static MLModule_ProvideImagePhashGeneratorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImagePhashGenerator provideImagePhashGenerator() {
    return Preconditions.checkNotNullFromProvides(MLModule.INSTANCE.provideImagePhashGenerator());
  }

  private static final class InstanceHolder {
    private static final MLModule_ProvideImagePhashGeneratorFactory INSTANCE = new MLModule_ProvideImagePhashGeneratorFactory();
  }
}
