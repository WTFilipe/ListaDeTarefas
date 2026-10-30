package com.foli.listadetarefas.ui.feature.editor.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.UiText

/**
 * Campos do formulário S2: título obrigatório (`singleLine`) e descrição opcional
 * (`minLines = 4`). Sem contadores (US-02). Mensagens sempre de recursos; erro
 * inline usa `error` e mantém o conteúdo digitado.
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
            Text(text = titleErrorText ?: stringResource(R.string.field_required))
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
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}
