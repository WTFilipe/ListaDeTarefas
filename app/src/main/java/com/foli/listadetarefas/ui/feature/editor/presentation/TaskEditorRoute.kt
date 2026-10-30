package com.foli.listadetarefas.ui.feature.editor.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Route da S2: coleta estado/efeitos, trata foco do campo título e navegação de
 * volta. Voltar/cancelar não persiste nada.
 */
@Composable
fun TaskEditorRoute(
    onNavigateBack: () -> Unit,
    viewModel: TaskEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TaskEditorEffect.NavigateBack -> onNavigateBack()
                is TaskEditorEffect.RequestFieldFocus -> when (effect.field) {
                    EditorField.TITLE -> titleFocusRequester.requestFocus()
                }
            }
        }
    }

    TaskEditorScreen(
        state = state,
        titleFocusRequester = titleFocusRequester,
        onTitleChanged = { viewModel.onAction(TaskEditorAction.TitleChanged(it)) },
        onDescriptionChanged = { viewModel.onAction(TaskEditorAction.DescriptionChanged(it)) },
        onBack = onNavigateBack,
        onSave = { viewModel.onAction(TaskEditorAction.SaveClicked) },
        onRetrySave = { viewModel.onAction(TaskEditorAction.RetrySaveClicked) },
        onRetryLoadTask = { viewModel.onAction(TaskEditorAction.RetryLoadTaskClicked) },
    )
}
