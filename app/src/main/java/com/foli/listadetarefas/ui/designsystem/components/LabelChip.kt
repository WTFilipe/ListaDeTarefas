package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.model.LabelUiModel
import com.foli.listadetarefas.ui.theme.AppShapes
import com.foli.listadetarefas.ui.theme.LocalLabelPalette

/**
 * Chip informativo da etiqueta na linha da tarefa (US-04 AC 10–12): container e
 * conteúdo da paleta fixa, dot + nome sempre visível, `labelMedium`, uma linha
 * com reticências. `null` não compõe nada (sem espaço reservado). O
 * `contentDescription` anuncia o nome completo mesmo quando o texto é truncado.
 */
@Composable
fun LabelChip(
    label: LabelUiModel?,
    modifier: Modifier = Modifier,
) {
    if (label == null) return
    val colors = LocalLabelPalette.current.colorsFor(label.colorKey)
    val description = stringResource(R.string.a11y_label, label.name)

    Surface(
        modifier = modifier.semantics { contentDescription = description },
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.onContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Spacer(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = colors.dot, shape = AppShapes.full),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
