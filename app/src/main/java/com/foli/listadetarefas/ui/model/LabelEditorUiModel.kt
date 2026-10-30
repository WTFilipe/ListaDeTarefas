package com.foli.listadetarefas.ui.model

import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.ui.UiText

/**
 * Estado do sheet S4 (criar/editar etiqueta). `labelId == null` significa
 * criação; a primeira cor da paleta é a pré-seleção (US-04 AC 2).
 */
data class LabelEditorUiModel(
    val labelId: String? = null,
    val name: String = "",
    val colorKey: LabelColorKey = LabelColorKey.DEFAULT,
    val nameError: UiText? = null,
)
