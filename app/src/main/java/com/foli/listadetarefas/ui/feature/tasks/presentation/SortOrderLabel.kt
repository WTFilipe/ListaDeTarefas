package com.foli.listadetarefas.ui.feature.tasks.presentation

import androidx.annotation.StringRes
import com.foli.listadetarefas.R
import com.foli.listadetarefas.domain.model.SortOrder

/**
 * Copy localizada das opções de ordenação (DESIGN_SPEC §11). A tradução do enum
 * para texto pertence à UI; o domínio permanece Kotlin puro. A ordem de leitura
 * do sheet segue a ordem das declarações de [SortOrder].
 */
@StringRes
fun SortOrder.labelRes(): Int = when (this) {
    SortOrder.TITLE_ASC -> R.string.sort_title_az
    SortOrder.TITLE_DESC -> R.string.sort_title_za
    SortOrder.CREATED_DESC -> R.string.sort_created_desc
    SortOrder.CREATED_ASC -> R.string.sort_created_asc
    SortOrder.DEADLINE_ASC -> R.string.sort_deadline_asc
    SortOrder.DEADLINE_DESC -> R.string.sort_deadline_desc
}
