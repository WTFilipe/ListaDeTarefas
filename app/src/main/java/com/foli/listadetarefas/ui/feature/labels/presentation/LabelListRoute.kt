package com.foli.listadetarefas.ui.feature.labels.presentation

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foli.listadetarefas.R
import com.foli.listadetarefas.ui.UiText
import kotlinx.coroutines.delay

/**
 * Route da S3: coleta estado/efeitos, trata snackbar de retry, foco do nome no
 * S4 e navegação de volta.
 */
@Composable
fun LabelListRoute(
    onNavigateBack: () -> Unit,
    viewModel: LabelListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val nameFocusRequester = remember { FocusRequester() }
    val resources = LocalResources.current
    val retryLabel = stringResource(R.string.retry)

    LaunchedEffect(viewModel, resources, retryLabel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LabelListEffect.NavigateBack -> onNavigateBack()
                is LabelListEffect.RequestNameFocus -> Unit
                is LabelListEffect.ShowStorageError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message.asString(resources),
                        actionLabel = retryLabel,
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.onAction(LabelListAction.RetryRequested(effect.operation))
                    }
                }
            }
        }
    }

    LaunchedEffect(state.editor?.labelId) {
        if (state.editor != null) {
            delay(150)
            runCatching { nameFocusRequester.requestFocus() }
        }
    }

    LabelListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        nameFocusRequester = nameFocusRequester,
        onCreate = { viewModel.onAction(LabelListAction.CreateClicked) },
        onLabelClick = { viewModel.onAction(LabelListAction.LabelClicked(it)) },
        onDeleteRequest = { viewModel.onAction(LabelListAction.DeleteRequested(it)) },
        onDeleteConfirm = { viewModel.onAction(LabelListAction.DeleteConfirmed) },
        onDeleteDismiss = { viewModel.onAction(LabelListAction.DeleteDismissed) },
        onRetryLoad = { viewModel.onAction(LabelListAction.RetryLoadClicked) },
        onBack = onNavigateBack,
        onEditorNameChange = { viewModel.onAction(LabelListAction.EditorNameChanged(it)) },
        onEditorColorSelected = { viewModel.onAction(LabelListAction.EditorColorSelected(it)) },
        onEditorSave = { viewModel.onAction(LabelListAction.EditorSaveClicked) },
        onEditorCancel = { viewModel.onAction(LabelListAction.EditorCancelClicked) },
    )
}

private fun UiText.asString(resources: Resources): String = when (this) {
    is UiText.Plain -> value
    is UiText.Res -> resources.getString(resId, *args.toTypedArray())
}
