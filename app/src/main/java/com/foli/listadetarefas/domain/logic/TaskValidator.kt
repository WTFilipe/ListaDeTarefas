package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.domain.model.TaskLimits

/** Texto de tarefa já normalizado (trim aplicado). */
data class NormalizedTaskText(
    val title: String,
    val description: String,
)

/**
 * Regras puras de normalização/validação da tarefa. Título vazio ou composto
 * apenas por espaços/quebras de linha é rejeitado; descrição só com espaços vira
 * vazia. Títulos iguais entre tarefas distintas são permitidos.
 *
 * Os limites de tamanho são defesa em profundidade: a apresentação trunca a
 * entrada (`limitTitle`/`limitDescription`), mas o domínio rejeita qualquer
 * payload acima de [TaskLimits.TITLE_MAX]/[TaskLimits.DESCRIPTION_MAX].
 */
object TaskValidator {

    fun normalizeTitle(raw: String): String = raw.trim()

    fun normalizeDescription(raw: String): String = raw.trim()

    /** Trunca o título ao máximo aceito (digitação/colagem). */
    fun limitTitle(raw: String): String = raw.take(TaskLimits.TITLE_MAX)

    /** Trunca a descrição ao máximo aceito (digitação/colagem). */
    fun limitDescription(raw: String): String = raw.take(TaskLimits.DESCRIPTION_MAX)

    fun validate(title: String, description: String): AppResult<NormalizedTaskText> {
        val normalizedTitle = normalizeTitle(title)
        if (normalizedTitle.isEmpty()) {
            return AppResult.Failure(AppFailure.EmptyTitle)
        }
        val normalizedDescription = normalizeDescription(description)
        if (normalizedTitle.length > TaskLimits.TITLE_MAX ||
            normalizedDescription.length > TaskLimits.DESCRIPTION_MAX
        ) {
            return AppResult.Failure(AppFailure.TextTooLong)
        }
        return AppResult.Success(
            NormalizedTaskText(
                title = normalizedTitle,
                description = normalizedDescription,
            ),
        )
    }
}
