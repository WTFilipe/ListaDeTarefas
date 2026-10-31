package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.text.TextNormalizer
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.model.TaskTab

/**
 * Filtros puros da lista.
 *
 * - `byTab` separa as tarefas ativas das concluídas (US-03 AC 1–4).
 * - `filterByQuery` aplica a busca da US-06: substring no título **ou** na
 *   descrição, ignorando caixa e acentos e removendo espaços das extremidades da
 *   consulta (`TextNormalizer`, ADR-0008). A consulta vazia devolve a lista
 *   original, sem tocar na ordem recebida, para que a ordenação vigente
 *   (US-01/US-07) continue sendo a única responsável pela ordem.
 *
 * Nenhuma das funções escreve, cria ou exclui dados: são funções puras, sem
 * Android, prontas para compor `aba -> busca -> ordenação`.
 */
object TaskFilter {

    fun active(tasks: List<Task>): List<Task> = byTab(tasks, TaskTab.ACTIVE)

    fun completed(tasks: List<Task>): List<Task> = byTab(tasks, TaskTab.COMPLETED)

    fun byTab(tasks: List<Task>, tab: TaskTab): List<Task> = when (tab) {
        TaskTab.ACTIVE -> tasks.filter { it.completedAt == null }
        TaskTab.COMPLETED -> tasks.filter { it.completedAt != null }
    }

    /**
     * Mantém as tarefas cujo título ou descrição contém a consulta. A busca é
     * literal (nenhum caractere tem significado especial) e estável: a ordem
     * relativa das tarefas que passam no filtro é a da lista de entrada.
     */
    fun filterByQuery(tasks: List<Task>, query: String): List<Task> {
        val needle = TextNormalizer.normalize(query)
        if (needle.isEmpty()) return tasks
        return tasks.filter { task -> matches(task, needle) }
    }

    private fun matches(task: Task, needle: String): Boolean =
        TextNormalizer.normalize(task.title).contains(needle) ||
            TextNormalizer.normalize(task.description).contains(needle)
}
