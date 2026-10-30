package com.foli.listadetarefas.domain.repository

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistência de tarefas. Leitura reativa por [Flow]; escritas
 * devolvem [AppResult] sem vazar exceções de infraestrutura.
 */
interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeTask(id: String): Flow<Task?>
    suspend fun getTaskById(id: String): AppResult<Task?>
    suspend fun upsert(task: Task): AppResult<Unit>
    suspend fun deleteById(id: String): AppResult<Unit>
}
