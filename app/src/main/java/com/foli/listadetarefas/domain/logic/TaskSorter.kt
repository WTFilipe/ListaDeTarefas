package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.Task

/**
 * Ordenação vigente da US-01: criação mais recente primeiro, com desempate
 * determinístico por `id`. O seletor de 6 ordenações pertence à US-07.
 */
object TaskSorter {
    fun sort(tasks: List<Task>): List<Task> =
        tasks.sortedWith(compareByDescending<Task> { it.createdAt }.thenBy { it.id })
}
