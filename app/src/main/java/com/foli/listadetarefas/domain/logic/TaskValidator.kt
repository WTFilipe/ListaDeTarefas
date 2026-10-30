package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult

/** Texto de tarefa já normalizado (trim aplicado). */
data class NormalizedTaskText(
    val title: String,
    val description: String,
)

/**
 * Regras puras de normalização/validação da tarefa. Título vazio ou composto
 * apenas por espaços/quebras de linha é rejeitado; descrição só com espaços vira
 * vazia. Títulos iguais entre tarefas distintas são permitidos.
 */
object TaskValidator {

    fun normalizeTitle(raw: String): String = raw.trim()

    fun normalizeDescription(raw: String): String = raw.trim()

    fun validate(title: String, description: String): AppResult<NormalizedTaskText> {
        val normalizedTitle = normalizeTitle(title)
        if (normalizedTitle.isEmpty()) {
            return AppResult.Failure(AppFailure.EmptyTitle)
        }
        return AppResult.Success(
            NormalizedTaskText(
                title = normalizedTitle,
                description = normalizeDescription(description),
            ),
        )
    }
}
