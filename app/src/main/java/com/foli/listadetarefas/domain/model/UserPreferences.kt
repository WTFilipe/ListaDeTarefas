package com.foli.listadetarefas.domain.model

/**
 * Preferências de interface persistidas (DataStore). A US-03 persiste apenas a
 * aba selecionada; a ordenação entra na US-07.
 */
data class UserPreferences(
    val selectedTab: TaskTab = TaskTab.ACTIVE,
)
