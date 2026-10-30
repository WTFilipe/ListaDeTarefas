package com.foli.listadetarefas.data.repository

import com.foli.listadetarefas.data.local.preferences.UserPreferencesDataSource
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences
import com.foli.listadetarefas.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Implementação DataStore do [PreferencesRepository]. */
class PreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : PreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> = dataSource.preferences

    override suspend fun setSelectedTab(tab: TaskTab) = dataSource.setSelectedTab(tab)
}
