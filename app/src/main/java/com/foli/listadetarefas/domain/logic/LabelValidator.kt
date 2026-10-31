package com.foli.listadetarefas.domain.logic

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.core.result.AppFailure
import com.foli.listadetarefas.core.result.AppResult
import com.foli.listadetarefas.core.text.TextNormalizer
import com.foli.listadetarefas.domain.model.TaskLimits

/** Nome de etiqueta já normalizado para exibição e unicidade. */
data class NormalizedLabelName(
    val name: String,
    val nameKey: String,
)

/**
 * Regras puras do nome de etiqueta (US-04 AC 2 e 3): trim, limite de 30 e
 * unicidade por chave sem caixa/acentos. O domínio nunca conhece `R.string`.
 */
object LabelValidator {

    fun normalizeName(raw: String): String = raw.trim()

    fun nameKey(name: String): String = TextNormalizer.normalize(name)

    /** Trunca a digitação/colagem ao limite de 30 caracteres. */
    fun limitName(raw: String): String = raw.take(TaskLimits.LABEL_NAME_MAX)

    fun validate(raw: String): AppResult<NormalizedLabelName> {
        val normalized = normalizeName(raw)
        if (normalized.isEmpty()) {
            return AppResult.Failure(AppFailure.EmptyLabelName)
        }
        if (normalized.length > TaskLimits.LABEL_NAME_MAX) {
            return AppResult.Failure(AppFailure.TextTooLong)
        }
        return AppResult.Success(
            NormalizedLabelName(
                name = normalized,
                nameKey = nameKey(normalized),
            ),
        )
    }

    /** Valida a combinação nome + cor fixa; não existe estado "sem cor". */
    fun validateWithColor(raw: String, colorKey: LabelColorKey): AppResult<LabelDraftValidated> =
        when (val name = validate(raw)) {
            is AppResult.Failure -> name
            is AppResult.Success -> AppResult.Success(
                LabelDraftValidated(name.data.name, name.data.nameKey, colorKey),
            )
        }
}

data class LabelDraftValidated(
    val name: String,
    val nameKey: String,
    val colorKey: LabelColorKey,
)
