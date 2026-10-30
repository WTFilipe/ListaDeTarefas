package com.foli.listadetarefas.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO reativo da tabela `task`. Leituras como [Flow] (cobrem o AC 9 de
 * persistência/observação); escritas por `upsert`/delete por id.
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM task ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id = :id")
    fun observeById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Upsert
    suspend fun upsert(entity: TaskEntity)

    @Query("DELETE FROM task WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Conclusão/reabertura idempotente: altera apenas `completed_at` (US-03). */
    @Query("UPDATE task SET completed_at = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: String, completedAt: Long?)

    /** Associação/remoção de etiqueta: altera apenas `label_id` (US-04). */
    @Query("UPDATE task SET label_id = :labelId WHERE id = :id")
    suspend fun setLabel(id: String, labelId: String?)

    /** Usado na exclusão de etiqueta para deixar as tarefas sem etiqueta. */
    @Query("UPDATE task SET label_id = NULL WHERE label_id = :labelId")
    suspend fun clearLabel(labelId: String)
}
