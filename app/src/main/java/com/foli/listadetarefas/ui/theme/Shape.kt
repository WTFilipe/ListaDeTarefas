package com.foli.listadetarefas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Tokens de shape do design system. `MaterialTheme.shapes` expõe apenas
 * extraSmall..extraLarge; `none` e `full` são definidos UMA única vez aqui e
 * expostos por `AppShapes`.
 */
object AppShapes {
    val none = RoundedCornerShape(0.dp)
    val full = RoundedCornerShape(percent = 50)

    /** Escala MD3 passada ao `MaterialTheme.shapes`. */
    val material = Shapes()
}
