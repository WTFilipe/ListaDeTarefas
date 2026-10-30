package com.foli.listadetarefas.domain.model

import java.time.LocalDate

/**
 * Tarefa persistida. `createdAt`/`completedAt` são epoch millis e `dueDate` é a
 * data limite opcional (somente data). Nenhum tipo Android neste pacote.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val labelId: String? = null,
    val dueDate: LocalDate? = null,
    val createdAt: Long,
    val completedAt: Long? = null,
)
