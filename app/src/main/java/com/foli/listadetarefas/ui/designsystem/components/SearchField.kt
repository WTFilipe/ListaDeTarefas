package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.theme.AppShapes

/**
 * Campo de busca compacto da S1 (DESIGN_SPEC §2.2, §9.7): container
 * `surfaceContainerHigh`, shape `full`, lupa decorativa e ação de limpar só
 * quando há consulta. O "limpar" mantém alvo de 48dp e `contentDescription`
 * próprio (DESIGN_SPEC §10); o placeholder nunca é hardcoded.
 *
 * O campo não confirma nada: cada caractere sobe por [onQueryChange] e o filtro
 * da lista é recalculado durante a digitação (US-06 AC 3).
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clearDescription = stringResource(R.string.search_clear)
    val placeholder = stringResource(R.string.search_placeholder)

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = clearDescription,
                    )
                }
            }
        },
        singleLine = true,
        shape = AppShapes.full,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    )
}
