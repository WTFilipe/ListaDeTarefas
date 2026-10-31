package com.foli.listadetarefas.domain.repository

import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.TaskLabel
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de persistência de etiquetas. Leituras são reativas e escritas não
 * vazam exceções de infraestrutura. A exclusão é responsável por deixar as
 * tarefas que usam a etiqueta com `labelId = null` sem apagar outros dados.
 */
interface LabelRepository {
    fun observeLabels(): Flow<List<TaskLabel>>
    suspend fun getLabelById(id: String): AppResult<TaskLabel?>
    suspend fun getByNormalizedNameKey(nameKey: String): AppResult<TaskLabel?>
    suspend fun upsert(label: TaskLabel): AppResult<Unit>
    suspend fun deleteById(id: String): AppResult<Unit>
}
