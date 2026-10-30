package com.foli.listadetarefas.domain.usecase.task

import com.foli.listadetarefas.domain.model.Task
import com.foli.listadetarefas.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observa uma tarefa por id; `null` quando ela não existe mais. */
class ObserveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    operator fun invoke(taskId: String): Flow<Task?> = repository.observeTask(taskId)
}
