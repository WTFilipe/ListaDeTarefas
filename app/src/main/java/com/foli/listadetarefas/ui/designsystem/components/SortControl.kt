package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons

/**
 * Botão de ordenação da S1 (DESIGN_SPEC §2.2/§9/§10): texto + ícones de sort e
 * expansão, `labelLarge` em `onSurfaceVariant`, altura mínima de 40dp e **até 2
 * linhas**, crescendo em altura a 200% de fonte em vez de cortar o nome da
 * ordenação vigente.
 *
 * A semântica anuncia "Ordenação atual: <nome>" (contentDescription completo),
 * independentemente do truncamento visual. O nome da opção entra separado de
 * [label] porque o texto visível já o prefixa com "Ordenação:"; compor a
 * semântica a partir de [label] duplicaria o rótulo (DESIGN_SPEC §10/§11).
 */
@Composable
fun SortControl(
    label: String,
    currentOption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.a11y_sort_current, currentOption)

    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = AppIcons.Sort,
                contentDescription = null,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
            )
            Icon(
                imageVector = AppIcons.ExpandMore,
                contentDescription = null,
            )
        }
    }
}
