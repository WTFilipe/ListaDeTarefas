package com.foli.listadetarefas.domain.model

import com.foli.listadetarefas.core.palette.LabelColorKey

/**
 * Etiqueta persistida. `name` é o texto como o usuário digitou (trim) e
 * `nameKey` é a forma normalizada (caixa baixa e sem acentos) usada na
 * unicidade. `colorKey` é uma chave da paleta fixa, sem tipo Android.
 */
data class TaskLabel(
    val id: String,
    val name: String,
    val nameKey: String,
    val colorKey: LabelColorKey,
    val createdAt: Long,
)
