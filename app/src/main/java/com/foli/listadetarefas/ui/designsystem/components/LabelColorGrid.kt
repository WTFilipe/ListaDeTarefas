package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.theme.AppShapes
import com.foli.listadetarefas.ui.theme.LocalLabelPalette

/**
 * Grade da paleta fixa do S4: 8 amostras em 4 colunas, alvo mínimo de 48dp,
 * `role = RadioButton` e seleção indicada por anel + check (não só por cor).
 */
@Composable
fun LabelColorGrid(
    selectedColorKey: LabelColorKey,
    onColorSelected: (LabelColorKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalLabelPalette.current
    val rows = palette.styles.chunked(4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowStyles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowStyles.forEach { style ->
                    val name = stringResource(style.nameRes)
                    val colorDescription = stringResource(R.string.a11y_label_color, name)
                    val selected = style.key == selectedColorKey
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onColorSelected(style.key) },
                            )
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small,
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .padding(vertical = 6.dp)
                            .semantics { contentDescription = colorDescription },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color = style.colors.dot, shape = AppShapes.full),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = AppShapes.full,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Check,
                                        contentDescription = stringResource(R.string.a11y_selected),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
