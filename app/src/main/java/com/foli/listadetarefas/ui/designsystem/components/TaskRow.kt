package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.model.TaskUiModel

/**
 * Linha da tarefa: checkbox de conclusão com alvo de 48dp, título e descrição em
 * até 2 linhas com reticências, sem segundo texto quando não há descrição e, para
 * tarefas concluídas, título riscado + marca "Concluída em dd/MM/aaaa". A
 * semântica carrega o texto **integral** (mergeDescendants + contentDescription)
 * para que o truncamento visual não perca informação no TalkBack. O menu ⋮ expõe
 * "Editar" e "Excluir".
 */
@Composable
fun TaskRow(
    task: TaskUiModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = task.title
    val description = task.description
    val baseSemanticsText = if (description.isBlank()) {
        title
    } else {
        stringResource(R.string.a11y_task, title, description)
    }
    val completedLabel = task.completedOnDate?.let {
        stringResource(R.string.completed_on_a11y, it)
    }
    val semanticsText = completedLabel?.let {
        stringResource(R.string.a11y_task, baseSemanticsText, it)
    } ?: baseSemanticsText
    val editLabel = stringResource(R.string.a11y_edit_task)
    val checkboxState = stringResource(
        if (task.isCompleted) R.string.a11y_checkbox_completed else R.string.a11y_checkbox_not_completed,
    )
    val toggleLabel = stringResource(
        if (task.isCompleted) R.string.a11y_reopen else R.string.a11y_mark_completed,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onToggle,
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    stateDescription = checkboxState
                    onClick(label = toggleLabel) {
                        onToggle(!task.isCompleted)
                        true
                    }
                },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClickLabel = editLabel, onClick = onClick)
                .semantics(mergeDescendants = true) {
                    contentDescription = semanticsText
                }
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (task.isCompleted) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (task.completedOnDate != null) {
                Text(
                    text = stringResource(R.string.completed_on, task.completedOnDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Box {
            var menuExpanded by remember { mutableStateOf(false) }
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = AppIcons.MoreVert,
                    contentDescription = stringResource(R.string.a11y_more_options),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.menu_edit)) },
                    leadingIcon = {
                        Icon(
                            imageVector = AppIcons.Edit,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}
