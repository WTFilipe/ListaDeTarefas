package com.foli.listadetarefas.domain.model

import java.time.LocalDate

/**
 * Payload de criação/edição. O `id` é gerado uma única vez quando o formulário é
 * aberto, garantindo escrita idempotente (repetir "Tentar novamente" não duplica).
 */
data class TaskDraft(
    val id: String,
    val title: String,
    val description: String,
    val labelId: String? = null,
    val dueDate: LocalDate? = null,
)
