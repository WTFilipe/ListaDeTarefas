package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.model.DeadlineUiState
import com.foli.listadetarefas.ui.theme.AppShapes

/**
 * Indicador de proximidade da data limite (US-05 AC 3 e 4). Sempre combina
 * texto + ícone (a cor é reforço, nunca o único sinal) e nunca é truncado.
 * Um único badge por tarefa; `deadline == null` não compõe nada.
 */
@Composable
fun DeadlineBadge(
    deadline: DeadlineUiState?,
    modifier: Modifier = Modifier,
) {
    if (deadline == null) return

    val container: Color
    val content: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val label: String
    var border: BorderStroke? = null
    when (deadline) {
        DeadlineUiState.OVERDUE -> {
            container = MaterialTheme.colorScheme.errorContainer
            content = MaterialTheme.colorScheme.onErrorContainer
            icon = AppIcons.Warning
            label = stringResource(R.string.deadline_overdue)
        }
        DeadlineUiState.TODAY -> {
            container = MaterialTheme.colorScheme.tertiaryContainer
            content = MaterialTheme.colorScheme.onTertiaryContainer
            icon = AppIcons.Event
            label = stringResource(R.string.deadline_today)
        }
        DeadlineUiState.TOMORROW -> {
            container = MaterialTheme.colorScheme.secondaryContainer
            content = MaterialTheme.colorScheme.onSecondaryContainer
            icon = AppIcons.Event
            label = stringResource(R.string.deadline_tomorrow)
        }
        DeadlineUiState.SOON -> {
            container = MaterialTheme.colorScheme.surfaceContainerHigh
            content = MaterialTheme.colorScheme.onSurfaceVariant
            icon = AppIcons.Schedule
            label = stringResource(R.string.deadline_soon)
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    }

    Surface(
        modifier = modifier.heightIn(min = 24.dp),
        shape = AppShapes.full,
        color = container,
        contentColor = content,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = content,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}
