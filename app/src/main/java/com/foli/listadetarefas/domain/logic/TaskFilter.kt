package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.domain.model.Task

/**
 * Filtro da lista ativa (tarefas ainda não concluídas). A aba Concluídas
 * pertence à US-03, mas a regra já vive aqui para não duplicar lógica.
 */
object TaskFilter {
    fun active(tasks: List<Task>): List<Task> = tasks.filter { it.completedAt == null }
}
