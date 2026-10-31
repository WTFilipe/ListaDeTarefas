package com.foli.listadetarefas.domain.model

/**
 * Preferências de interface persistidas (DataStore). A US-03 persistiu a aba
 * selecionada; a US-07 acrescenta a ordenação escolhida (default: criação mais
 * recente primeiro).
 */
data class UserPreferences(
    val selectedTab: TaskTab = TaskTab.ACTIVE,
    val sortOrder: SortOrder = SortOrder.DEFAULT,
)
