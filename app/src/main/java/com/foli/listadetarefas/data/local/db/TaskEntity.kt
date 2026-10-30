package com.foli.listadetarefas.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room da tabela `task` (ARCHITECTURE.md §9.1).
 *
 * `due_date` é `epochDay` e `created_at`/`completed_at` são `epoch millis`; não há
 * `TypeConverter`. Os campos de stories futuras (`label_id`, `due_date`,
 * `completed_at`) já existem no schema v1 como anuláveis e permanecem inativos
 * nesta história.
 */
@Entity(
    tableName = "task",
    indices = [
        Index(value = ["label_id"]),
        Index(value = ["created_at"]),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "label_id") val labelId: String? = null,
    @ColumnInfo(name = "due_date") val dueDate: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
)
