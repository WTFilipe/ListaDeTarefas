package com.foli.listadetarefas.ui.feature.editor.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.foli.listadetarefas.R
import com.foli.listadetarefas.domain.model.TaskLimits
import com.foli.listadetarefas.ui.UiText

/**
 * Campos do formulário S2: título obrigatório (`singleLine`, 120 caracteres) e
 * descrição opcional (`minLines = 4`, `maxLines = 6`, 2000 caracteres).
 *
 * O `supportingText` sempre mostra o contador `atual/máximo` à direita
 * (`bodySmall`, `onSurfaceVariant`); o apoio/erro fica à esquerda e, ao atingir
 * o máximo, passa a "Limite atingido". Erro de validação tem precedência sobre o
 * texto de limite. Textos sempre de recursos (DESIGN_SPEC §9.4).
 */
@Composable
fun TaskFormFields(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    titleError: UiText?,
    titleFocusRequester: FocusRequester,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val titleErrorText = when (titleError) {
        null -> null
        is UiText.Plain -> titleError.value
        is UiText.Res -> stringResource(titleError.resId, *titleError.args.toTypedArray())
    }
    val titleAtLimit = title.length >= TaskLimits.TITLE_MAX
    val titleSupportText = when {
        titleErrorText != null -> titleErrorText
        titleAtLimit -> stringResource(R.string.support_limit_reached)
        else -> stringResource(R.string.field_required)
    }
    val descriptionAtLimit = description.length >= TaskLimits.DESCRIPTION_MAX

    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(titleFocusRequester),
        enabled = enabled,
        label = { Text(text = stringResource(R.string.task_title_label)) },
        singleLine = true,
        isError = titleError != null,
        supportingText = {
            FieldSupportingText(
                supportText = titleSupportText,
                count = title.length,
                max = TaskLimits.TITLE_MAX,
                isError = titleError != null,
            )
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )

    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(text = stringResource(R.string.task_description_label_optional)) },
        minLines = 4,
        maxLines = 6,
        supportingText = {
            FieldSupportingText(
                supportText = if (descriptionAtLimit) {
                    stringResource(R.string.support_limit_reached)
                } else {
                    null
                },
                count = description.length,
                max = TaskLimits.DESCRIPTION_MAX,
                isError = false,
            )
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

/**
 * Linha de apoio do text field: mensagem/erro à esquerda (quando houver) e
 * contador `atual/máximo` à direita. O contador nunca usa `error`.
 */
@Composable
private fun FieldSupportingText(
    supportText: String?,
    count: Int,
    max: Int,
    isError: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (supportText == null) {
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Text(
                text = supportText,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Text(
            text = stringResource(R.string.counter_format, count, max),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
