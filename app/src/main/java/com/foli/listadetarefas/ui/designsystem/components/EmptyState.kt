package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons

/**
 * Estado vazio/prioritário de uma tela: halo com ícone, título, eco opcional,
 * corpo e ação primária opcional. O corpo é rolável para que a ação continue
 * alcançável com fonte a 200% (DESIGN_SPEC §8.2).
 *
 * `echo` existe para o empty de busca (US-06 AC 6): visualmente limitado a 2
 * linhas com reticências, mas anunciado **integralmente** no `contentDescription`
 * (DESIGN_SPEC §4 e §10) — é a única exceção à regra de não truncar em empty
 * states. `isActionTonal` atende à ação "Limpar busca" (botão tonal); os empties
 * existentes continuam usando o botão preenchido. Todos os textos chegam prontos
 * vindos de recursos.
 */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    icon: ImageVector = AppIcons.CheckCircle,
    echo: String? = null,
    isActionTonal: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (echo != null) {
            Text(
                text = echo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .semantics { contentDescription = echo },
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        if (actionLabel != null) {
            if (isActionTonal) {
                FilledTonalButton(onClick = onAction) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(onClick = onAction) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
