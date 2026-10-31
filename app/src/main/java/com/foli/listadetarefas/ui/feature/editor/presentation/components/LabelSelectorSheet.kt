package com.foli.listadetarefas.ui.feature.editor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.model.LabelUiModel
import com.foli.listadetarefas.ui.theme.AppShapes
import com.foli.listadetarefas.ui.theme.LocalLabelPalette

/**
 * S7 — único seletor de etiqueta da tarefa (modelo único). "Sem etiqueta" é
 * sempre a primeira opção; escolher aplica e fecha. O atalho "Criar etiqueta"
 * abre o S4 preservando o formulário (estado elevado no ViewModel/Route).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelSelectorSheet(
    labels: List<LabelUiModel>,
    selectedLabelId: String?,
    onSelect: (String?) -> Unit,
    onCreateShortcut: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.label_section),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            SelectorRow(
                selected = selectedLabelId == null,
                name = stringResource(R.string.label_none),
                dotColor = null,
                onClick = { onSelect(null) },
            )
            labels.forEach { label ->
                SelectorRow(
                    selected = selectedLabelId == label.id,
                    name = label.name,
                    dotColor = LocalLabelPalette.current.colorsFor(label.colorKey).dot,
                    onClick = { onSelect(label.id) },
                )
            }
            if (labels.isEmpty()) {
                Text(
                    text = stringResource(R.string.selector_empty_labels),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            TextButton(onClick = onCreateShortcut) {
                Text(text = stringResource(R.string.label_create_shortcut))
            }
        }
    }
}

@Composable
private fun SelectorRow(
    selected: Boolean,
    name: String,
    dotColor: androidx.compose.ui.graphics.Color?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color = dotColor, shape = AppShapes.full),
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
