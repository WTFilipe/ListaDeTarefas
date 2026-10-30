package com.foli.listadetarefas.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** DAO reativo da tabela `label` (S3/S4 da US-04). */
@Dao
interface LabelDao {

    @Query("SELECT * FROM label ORDER BY name_key ASC")
    fun observeAll(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM label WHERE id = :id")
    suspend fun getById(id: String): LabelEntity?

    @Query("SELECT * FROM label WHERE name_key = :nameKey LIMIT 1")
    suspend fun getByNameKey(nameKey: String): LabelEntity?

    @Upsert
    suspend fun upsert(entity: LabelEntity)

    @Query("DELETE FROM label WHERE id = :id")
    suspend fun deleteById(id: String)
}
