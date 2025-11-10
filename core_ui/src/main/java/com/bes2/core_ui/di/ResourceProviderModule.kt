package com.bes2.core_ui.di

import com.bes2.core_common.provider.ResourceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ResourceProviderModule {

    @Binds
    abstract fun bindResourceProvider(impl: DefaultResourceProvider): ResourceProvider
}
