package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.domain.logic.TaskFilter
import com.foli.listadetarefas.domain.logic.TaskSorter
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Observa a lista ativa já ordenada pela ordenação vigente da US-01. */
class ObserveTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> =
        repository.observeTasks().map { tasks ->
            TaskSorter.sort(TaskFilter.active(tasks))
        }
}
