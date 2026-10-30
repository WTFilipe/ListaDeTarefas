package com.foli.listadetarefas.ui.feature.editor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.designsystem.components.ActionTopAppBar
import com.foli.listadetarefas.ui.designsystem.components.ErrorBanner
import com.foli.listadetarefas.ui.feature.editor.presentation.components.TaskFormFields

/**
 * S2 stateless: corpo rolável com top app bar fixa, título obrigatório e
 * descrição opcional. Ações apenas na top app bar (`←` e "Salvar"). Não há ação
 * destrutiva nem rodapé de ações (DESIGN_SPEC §2.3).
 */
@Composable
fun TaskEditorScreen(
    state: TaskEditorState,
    titleFocusRequester: FocusRequester,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onRetrySave: () -> Unit,
    onRetryLoadTask: () -> Unit,
) {
    val screenTitle = when (state.mode) {
        is EditorMode.Create -> stringResource(R.string.new_task)
        is EditorMode.Edit -> stringResource(R.string.edit_task)
    }
    val saveLabel = if (state.isSaving) {
        stringResource(R.string.saving)
    } else {
        stringResource(R.string.save)
    }
    // Bloqueia edição e salvamento durante o carregamento e após falha de leitura:
    // salvar sem ver os dados atuais sobrescreveria a tarefa existente.
    val formEnabled = !state.isSaving && !state.isLoading && !state.taskLoadFailed

    Scaffold(
        topBar = {
            ActionTopAppBar(
                title = screenTitle,
                onBack = onBack,
                actions = {
                    TextButton(onClick = onSave, enabled = formEnabled) {
                        Text(text = saveLabel)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.taskLoadFailed -> ErrorBanner(
                    message = UiText.Res(R.string.task_load_error),
                    onRetry = onRetryLoadTask,
                )
                state.bannerError != null -> ErrorBanner(
                    message = state.bannerError,
                    onRetry = onRetrySave,
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            TaskFormFields(
                title = state.title,
                onTitleChange = onTitleChanged,
                description = state.description,
                onDescriptionChange = onDescriptionChanged,
                titleError = state.titleError,
                titleFocusRequester = titleFocusRequester,
                enabled = formEnabled,
            )
        }
    }
}
