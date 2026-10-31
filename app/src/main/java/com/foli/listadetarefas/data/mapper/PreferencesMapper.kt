package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences

/**
 * Conversão das preferências persistidas. Valores desconhecidos/corrompidos
 * caem nos defaults (`ACTIVE` / `CREATED_DESC`) sem lançar exceção
 * (ARCHITECTURE.md §9.2).
 */
object PreferencesMapper {

    fun toDomain(
        selectedTabName: String?,
        sortOrderName: String? = null,
    ): UserPreferences = UserPreferences(
        selectedTab = tabFromName(selectedTabName),
        sortOrder = sortOrderFromName(sortOrderName),
    )

    fun tabFromName(name: String?): TaskTab =
        TaskTab.entries.firstOrNull { it.name == name } ?: TaskTab.ACTIVE

    fun sortOrderFromName(name: String?): SortOrder = SortOrder.fromName(name)
}
