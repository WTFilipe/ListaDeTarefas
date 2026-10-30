package com.foli.listadetarefas.di

import com.foli.listadetarefas.data.repository.PreferencesRepositoryImpl
import com.foli.listadetarefas.data.repository.TaskRepositoryImpl
import com.foli.listadetarefas.domain.repository.PreferencesRepository
import com.foli.listadetarefas.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
