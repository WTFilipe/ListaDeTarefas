package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskTab

/**
 * Filtro por aba da lista. `ACTIVE` mantém as tarefas ainda não concluídas e
 * `COMPLETED` as que possuem data/hora de conclusão (US-03 AC 1–4). Função pura,
 * sem ordenação: a ordenação vigente é aplicada depois por `TaskSorter`.
 */
object TaskFilter {

    fun active(tasks: List<Task>): List<Task> = byTab(tasks, TaskTab.ACTIVE)

    fun completed(tasks: List<Task>): List<Task> = byTab(tasks, TaskTab.COMPLETED)

    fun byTab(tasks: List<Task>, tab: TaskTab): List<Task> = when (tab) {
        TaskTab.ACTIVE -> tasks.filter { it.completedAt == null }
        TaskTab.COMPLETED -> tasks.filter { it.completedAt != null }
    }
}
