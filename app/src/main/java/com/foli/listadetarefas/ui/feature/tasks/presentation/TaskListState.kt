package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.domain.model.SortOrder
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.ui.model.TaskUiModel

/**
 * Estado imutável da S1.
 *
 * - `query` é a consulta de busca (texto como o usuário digitou); vive no
 *   `SavedStateHandle` do ViewModel e sobrevive à navegação S1 ↔ S2/S3
 *   (`ARCHITECTURE.md` §9.2). A consulta vazia significa "sem filtro".
 * - `tab` controla a aba visível e persiste via DataStore.
 * - `sortOrder` é a ordenação vigente (default: criação mais recente primeiro)
 *   e também persiste via DataStore (US-07).
 * - `pendingDeleteId` controla a visibilidade do S6.
 * - `isLoading` só liga se a primeira emissão demorar mais de 200 ms;
 *   `loadFailed` sinaliza falha de leitura com ação "Tentar novamente".
 */
data class TaskListState(
    val query: String = "",
    val tab: TaskTab = TaskTab.ACTIVE,
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val rows: List<TaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val pendingDeleteId: String? = null,
)
