package com.foli.listadetarefas.data.mapper

import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.domain.model.UserPreferences

/**
 * Conversão das preferências persistidas. Valores desconhecidos/corrompidos
 * caem no default (`ACTIVE`) sem lançar exceção (ARCHITECTURE.md §9.2).
 */
object PreferencesMapper {

    fun toDomain(selectedTabName: String?): UserPreferences = UserPreferences(
        selectedTab = tabFromName(selectedTabName),
    )

    fun tabFromName(name: String?): TaskTab =
        TaskTab.entries.firstOrNull { it.name == name } ?: TaskTab.ACTIVE
}
