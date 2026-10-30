package com.foli.listadetarefas.ui.feature.tasks.presentation

import com.foli.listadetarefas.ui.model.TaskUiModel

/**
 * Estado imutável da S1. `pendingDeleteId` controla a visibilidade do S6;
 * `isLoading` só liga se a primeira emissão demorar mais de 200 ms; `loadFailed`
 * sinaliza falha de leitura com ação "Tentar novamente".
 */
data class TaskListState(
    val rows: List<TaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val pendingDeleteId: String? = null,
)
