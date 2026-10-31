package com.foli.listadetarefas.ui.feature.labels.presentation

import com.foli.listadetarefas.ui.model.LabelEditorUiModel
import com.foli.listadetarefas.ui.model.LabelUiModel

/**
 * Estado imutável da S3. `editor` controla a visibilidade do S4 e
 * `pendingDelete` a do dialog S6. `isLoading` só liga se a primeira emissão
 * demorar mais de 200 ms.
 */
data class LabelListState(
    val labels: List<LabelUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val editor: LabelEditorUiModel? = null,
    val pendingDelete: LabelUiModel? = null,
    val isSaving: Boolean = false,
)
