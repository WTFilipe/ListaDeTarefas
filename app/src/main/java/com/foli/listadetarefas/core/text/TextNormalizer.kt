package com.foli.listadetarefas.core.text

import java.text.Normalizer
import java.util.Locale

/**
 * Normalização de texto pt-BR para comparação/chave de unicidade
 * (`ARCHITECTURE.md` §9.1, ADR-0008): caixa baixa, sem acentos, com extremidades
 * sem espaços. Não altera o texto exibido ao usuário.
 */
object TextNormalizer {

    private val diacritics = Regex("\\p{M}+")

    fun normalize(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace(diacritics, "")
            .lowercase(Locale.forLanguageTag("pt-BR"))
}
