package com.foli.listadetarefas.ui.model

import com.foli.listadetarefas.core.palette.LabelColorKey

/**
 * Vocabulário de apresentação de etiqueta (`ARCHITECTURE.md` §4.1): apenas
 * primitivos e a chave de cor do core. O design system não conhece o domínio.
 */
data class LabelUiModel(
    val id: String,
    val name: String,
    val colorKey: LabelColorKey,
)
