package com.foli.listadetarefas.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room da tabela `label` (`ARCHITECTURE.md` §9.1). `name_key` é a forma
 * normalizada (caixa baixa/sem acentos) com índice único; `color_key` é o nome
 * da [com.foli.listadetarefas.core.palette.LabelColorKey].
 */
@Entity(
    tableName = "label",
    indices = [Index(value = ["name_key"], unique = true)],
)
data class LabelEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "name_key") val nameKey: String,
    @ColumnInfo(name = "color_key") val colorKey: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
