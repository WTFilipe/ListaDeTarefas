package com.foli.listadetarefas.di

import com.foli.listadetarefas.core.analytics.AnalyticsTracker
import com.foli.listadetarefas.core.analytics.NoOpAnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: NoOpAnalyticsTracker): AnalyticsTracker
}
