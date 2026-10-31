package com.foli.listadetarefas.domain.repository

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de preferências locais. A leitura é reativa e valores ausentes ou
 * desconhecidos caem nos defaults (`selectedTab = ACTIVE`,
 * `sortOrder = CREATED_DESC`).
 */
interface PreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun setSelectedTab(tab: TaskTab)
    suspend fun setSortOrder(order: SortOrder)
}
