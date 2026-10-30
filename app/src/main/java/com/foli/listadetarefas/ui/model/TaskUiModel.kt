package com.foli.listadetarefas.ui.model

/**
 * Vocabulário de apresentação da lista (`ARCHITECTURE.md` §4.1). Apenas
 * primitivos: o domínio é mapeado pela feature e o design system não conhece
 * tipos de domínio (`ui/model` não importa Compose nem domain). `completedOnDate`
 * já vem formatado como `dd/MM/aaaa` (US-03).
 */
data class TaskUiModel(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completedOnDate: String? = null,
)
