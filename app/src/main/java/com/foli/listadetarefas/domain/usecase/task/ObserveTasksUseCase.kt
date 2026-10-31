package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observa todas as tarefas **sem ordenar**. A ordem é responsabilidade da
 * projeção da S1 (`TaskFilter.byTab` -> busca -> `TaskSorter.sort(order)`), que
 * depende da ordenação escolhida e persistida (US-07). O escopo por aba também
 * é aplicado na projeção, pois a US-03 precisa das duas listas.
 */
class ObserveTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> = repository.observeTasks()
}
