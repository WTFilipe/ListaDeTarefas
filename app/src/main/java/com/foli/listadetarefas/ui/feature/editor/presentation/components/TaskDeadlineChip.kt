package com.foli.listadetarefas.ui.feature.editor.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.format.DateLabelFormatter
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.theme.AppShapes
import java.time.LocalDate

/**
 * Chip único da seção "Data limite" do S2 (US-05 AC 1 e 8): sem data mostra
 * "Definir data"; com data mostra `dd/MM/aaaa` + ação de remover. Nunca exibe os
 * dois estados ao mesmo tempo (DESIGN_SPEC §9.3).
 */
@Composable
fun TaskDeadlineChip(
    dueDate: LocalDate?,
    onSetClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (dueDate == null) {
        OutlinedButton(
            onClick = onSetClick,
            modifier = modifier.heightIn(min = 48.dp),
            enabled = enabled,
            shape = AppShapes.full,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = AppIcons.Calendar,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.deadline_action_set),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        return
    }

    val removeDescription = stringResource(R.string.deadline_remove)
    Surface(
        modifier = modifier.heightIn(min = 48.dp),
        shape = AppShapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = AppIcons.Calendar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(20.dp),
            )
            Text(
                text = DateLabelFormatter.formatDate(dueDate),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(48.dp),
                enabled = enabled,
            ) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = removeDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
