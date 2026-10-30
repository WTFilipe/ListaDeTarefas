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
}
