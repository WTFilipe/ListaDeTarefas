package com.foli.listadetarefas.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.model.LabelEditorUiModel

/**
 * S4 stateless (criar/editar etiqueta), reutilizado pela S3 e pelo atalho da S2.
 * O estado (nome, cor, erro) e as chamadas de domínio ficam no ViewModel da
 * feature; este composable apenas renderiza. O contador `atual/30` fica sempre
 * visível e o erro tem precedência sobre "Limite atingido".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelEditorSheet(
    state: LabelEditorUiModel,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onColorSelected: (LabelColorKey) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    maxNameLength: Int,
    modifier: Modifier = Modifier,
    nameFocusRequester: FocusRequester? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = if (state.labelId == null) {
        stringResource(R.string.label_new)
    } else {
        stringResource(R.string.label_edit)
    }
    val errorText = when (val error = state.nameError) {
        null -> null
        is UiText.Plain -> error.value
        is UiText.Res -> stringResource(error.resId, *error.args.toTypedArray())
    }
    val atLimit = state.name.length >= maxNameLength
    val supportText = when {
        errorText != null -> errorText
        atLimit -> stringResource(R.string.support_limit_reached)
        else -> stringResource(R.string.support_max_chars, maxNameLength)
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (nameFocusRequester != null) {
                            Modifier.focusRequester(nameFocusRequester)
                        } else {
                            Modifier
                        },
                    ),
                enabled = !isSaving,
                label = { Text(text = stringResource(R.string.label_name_label)) },
                singleLine = true,
                isError = errorText != null,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = supportText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (errorText != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = stringResource(
                                R.string.counter_format,
                                state.name.length,
                                maxNameLength,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            Text(
                text = stringResource(R.string.label_color_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            LabelColorGrid(
                selectedColorKey = state.colorKey,
                onColorSelected = onColorSelected,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onCancel, enabled = !isSaving) {
                    Text(text = stringResource(R.string.cancel))
                }
                Button(onClick = onSave, enabled = !isSaving) {
                    Text(
                        text = if (isSaving) {
                            stringResource(R.string.saving)
                        } else {
                            stringResource(R.string.save)
                        },
                    )
                }
            }
        }
    }
}
