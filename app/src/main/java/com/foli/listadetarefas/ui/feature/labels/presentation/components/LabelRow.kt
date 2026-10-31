package com.foli.listadetarefas.ui.feature.labels.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.model.LabelUiModel
import com.foli.listadetarefas.ui.theme.AppShapes
import com.foli.listadetarefas.ui.theme.LocalLabelPalette

/**
 * Linha da S3: amostra circular da cor, nome (`titleMedium`, 1 linha com
 * reticências), toque para editar e menu com editar/excluir. A cor nunca é o
 * único sinal — o nome acompanha a amostra.
 */
@Composable
fun LabelRow(
    label: LabelUiModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalLabelPalette.current.colorsFor(label.colorKey)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClickLabel = stringResource(R.string.label_edit), onClick = onClick)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color = colors.dot, shape = AppShapes.full),
        )
        Text(
            text = label.name,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box {
            var menuExpanded by remember { mutableStateOf(false) }
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = AppIcons.MoreVert,
                    contentDescription = stringResource(R.string.a11y_more_options_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.menu_edit)) },
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
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}
