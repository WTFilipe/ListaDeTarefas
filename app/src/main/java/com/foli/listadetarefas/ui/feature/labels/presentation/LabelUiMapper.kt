package com.foli.listadetarefas.ui.feature.labels.presentation

import com.foli.listadetarefas.domain.model.TaskLabel
import com.foli.listadetarefas.ui.model.LabelUiModel

/** Mapper domínio -> `ui/model` da feature de etiquetas. */
object LabelUiMapper {
    fun toUiModel(label: TaskLabel): LabelUiModel = LabelUiModel(
        id = label.id,
        name = label.name,
        colorKey = label.colorKey,
    )
}
