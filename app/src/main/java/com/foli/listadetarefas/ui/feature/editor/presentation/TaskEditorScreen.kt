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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foli.listadetarefas.R
import com.foli.listadetarefas.core.palette.LabelColorKey
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.designsystem.components.ActionTopAppBar
import com.foli.listadetarefas.ui.designsystem.components.ErrorBanner
import com.foli.listadetarefas.ui.designsystem.components.LabelEditorSheet
import com.foli.listadetarefas.ui.designsystem.components.LabelSelectorRow
import com.foli.listadetarefas.ui.feature.editor.presentation.components.DeadlineDatePickerDialog
import com.foli.listadetarefas.ui.feature.editor.presentation.components.LabelSelectorSheet
import com.foli.listadetarefas.ui.feature.editor.presentation.components.TaskDeadlineChip
import com.foli.listadetarefas.ui.feature.editor.presentation.components.TaskFormFields
import java.time.LocalDate

/**
 * S2 stateless: corpo rolável com top app bar fixa, título obrigatório e
 * descrição opcional. Ações apenas na top app bar (`←` e "Salvar"). Não há ação
 * destrutiva nem rodapé de ações (DESIGN_SPEC §2.3).
 */
@Composable
fun TaskEditorScreen(
    state: TaskEditorState,
    titleFocusRequester: FocusRequester,
    snackbarHostState: SnackbarHostState,
    showLabelSelector: Boolean,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onLabelClick: () -> Unit,
    onLabelSelectorDismiss: () -> Unit,
    onLabelSelected: (String?) -> Unit,
    onLabelCreateShortcut: () -> Unit,
    onLabelEditorNameChange: (String) -> Unit,
    onLabelEditorColorSelected: (LabelColorKey) -> Unit,
    onLabelEditorSave: () -> Unit,
    onLabelEditorCancel: () -> Unit,
    onRetryLoadLabels: () -> Unit,
    showDeadlinePicker: Boolean,
    onDeadlineSetClick: () -> Unit,
    onDeadlineRemove: () -> Unit,
    onDeadlinePickerDismiss: () -> Unit,
    onDeadlineSelected: (LocalDate) -> Unit,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            if (state.labelsLoadFailed) {
                ErrorBanner(
                    message = UiText.Res(R.string.labels_load_error),
                    onRetry = onRetryLoadLabels,
                )
            } else if (state.isLabelLoading) {
                Text(
                    text = stringResource(R.string.a11y_loading_labels),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(R.string.label_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            LabelSelectorRow(
                current = state.availableLabels.firstOrNull { it.id == state.selectedLabelId },
                onClick = onLabelClick,
            )

            Text(
                text = stringResource(R.string.deadline_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TaskDeadlineChip(
                dueDate = state.dueDate,
                onSetClick = onDeadlineSetClick,
                onRemoveClick = onDeadlineRemove,
                enabled = formEnabled,
            )
        }
    }

    state.labelEditor?.let { editor ->
        LabelEditorSheet(
            state = editor,
            isSaving = false,
            onNameChange = onLabelEditorNameChange,
            onColorSelected = onLabelEditorColorSelected,
            onSave = onLabelEditorSave,
            onCancel = onLabelEditorCancel,
            maxNameLength = com.foli.listadetarefas.domain.model.TaskLimits.LABEL_NAME_MAX,
        )
    }

    if (showLabelSelector) {
        LabelSelectorSheet(
            labels = state.availableLabels,
            selectedLabelId = state.selectedLabelId,
            onSelect = onLabelSelected,
            onCreateShortcut = onLabelCreateShortcut,
            onDismiss = onLabelSelectorDismiss,
        )
    }

    if (showDeadlinePicker) {
        DeadlineDatePickerDialog(
            initialDate = state.dueDate ?: state.today,
            onDateSelected = onDeadlineSelected,
            onDismiss = onDeadlinePickerDismiss,
        )
    }
}
