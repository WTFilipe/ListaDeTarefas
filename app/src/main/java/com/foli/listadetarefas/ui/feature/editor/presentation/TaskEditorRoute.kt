package com.foli.listadetarefas.ui.feature.editor.presentation

import android.content.res.Resources
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foli.listadetarefas.ui.UiText

/**
 * Route da S2: coleta estado/efeitos, trata foco do campo título, snackbar de
 * erro de etiqueta e navegação de volta. O S7 é UI-local (`rememberSaveable`);
 * o rascunho do S4 vive no ViewModel para preservar conteúdo em erro/atalho.
 */
@Composable
fun TaskEditorRoute(
    onNavigateBack: () -> Unit,
    viewModel: TaskEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val titleFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    var showLabelSelector by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel, resources) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TaskEditorEffect.NavigateBack -> onNavigateBack()
                is TaskEditorEffect.RequestFieldFocus -> when (effect.field) {
                    EditorField.TITLE -> titleFocusRequester.requestFocus()
                    EditorField.LABEL_NAME -> Unit
                }
                is TaskEditorEffect.ShowStorageError ->
                    snackbarHostState.showSnackbar(effect.message.asString(resources))
            }
        }
    }

    TaskEditorScreen(
        state = state,
        titleFocusRequester = titleFocusRequester,
        snackbarHostState = snackbarHostState,
        showLabelSelector = showLabelSelector,
        onTitleChanged = { viewModel.onAction(TaskEditorAction.TitleChanged(it)) },
        onDescriptionChanged = { viewModel.onAction(TaskEditorAction.DescriptionChanged(it)) },
        onLabelClick = { showLabelSelector = true },
        onLabelSelectorDismiss = { showLabelSelector = false },
        onLabelSelected = { labelId ->
            showLabelSelector = false
            viewModel.onAction(TaskEditorAction.LabelSelected(labelId))
        },
        onLabelCreateShortcut = {
            showLabelSelector = false
            viewModel.onAction(TaskEditorAction.LabelCreateShortcutClicked)
        },
        onLabelEditorNameChange = {
            viewModel.onAction(TaskEditorAction.LabelEditorNameChanged(it))
        },
        onLabelEditorColorSelected = {
            viewModel.onAction(TaskEditorAction.LabelEditorColorSelected(it))
        },
        onLabelEditorSave = { viewModel.onAction(TaskEditorAction.LabelEditorSaveClicked) },
        onLabelEditorCancel = { viewModel.onAction(TaskEditorAction.LabelEditorCancelClicked) },
        onRetryLoadLabels = { viewModel.onAction(TaskEditorAction.RetryLoadLabelsClicked) },
        onBack = onNavigateBack,
        onSave = { viewModel.onAction(TaskEditorAction.SaveClicked) },
        onRetrySave = { viewModel.onAction(TaskEditorAction.RetrySaveClicked) },
        onRetryLoadTask = { viewModel.onAction(TaskEditorAction.RetryLoadTaskClicked) },
    )
}

private fun UiText.asString(resources: Resources): String = when (this) {
    is UiText.Plain -> value
    is UiText.Res -> resources.getString(resId, *args.toTypedArray())
}
