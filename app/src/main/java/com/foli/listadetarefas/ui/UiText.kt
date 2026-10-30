package com.foli.listadetarefas.ui

import androidx.annotation.StringRes

/**
 * Texto de UI: recurso localizado ou texto plano. O domínio nunca usa este tipo.
 */
sealed interface UiText {
    data class Res(@param:StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText
    data class Plain(val value: String) : UiText
}
