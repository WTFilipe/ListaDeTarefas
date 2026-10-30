package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.domain.logic.TaskSorter
import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observa todas as tarefas já ordenadas pela ordenação vigente da US-01. O
 * escopo por aba (`TaskFilter.byTab`) é aplicado na projeção do ViewModel, pois a
 * US-03 precisa das duas listas.
 */
class ObserveTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> =
        repository.observeTasks().map(TaskSorter::sort)
}
