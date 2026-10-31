package com.foli.listadetarefas.ui.feature.labels.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.foli.listadetarefas.ui.designsystem.components.EmptyState
import com.foli.listadetarefas.ui.designsystem.components.ErrorBanner
import com.foli.listadetarefas.ui.designsystem.components.LabelEditorSheet
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.feature.labels.presentation.components.DeleteLabelDialog
import com.foli.listadetarefas.ui.feature.labels.presentation.components.LabelRow

/**
 * S3 stateless: lista de etiquetas com amostra de cor, menu e FAB. Sem etiquetas,
 * exibe o estado vazio com CTA própria e oculta o FAB (DESIGN_SPEC §8.2/§9.2).
 */
@Composable
fun LabelListScreen(
    state: LabelListState,
    snackbarHostState: SnackbarHostState,
    nameFocusRequester: FocusRequester,
    onCreate: () -> Unit,
    onLabelClick: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onRetryLoad: () -> Unit,
    onBack: () -> Unit,
    onEditorNameChange: (String) -> Unit,
    onEditorColorSelected: (LabelColorKey) -> Unit,
    onEditorSave: () -> Unit,
    onEditorCancel: () -> Unit,
) {
    val showFab = state.labels.isNotEmpty()

    Scaffold(
        topBar = {
            ActionTopAppBar(
                title = stringResource(R.string.labels_title),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab && !state.isLoading && !state.loadFailed) {
                ExtendedFloatingActionButton(
                    onClick = onCreate,
                    icon = {
                        Icon(imageVector = AppIcons.Add, contentDescription = null)
                    },
                    text = { Text(text = stringResource(R.string.label_new)) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.loadFailed -> ErrorBanner(
                    message = UiText.Res(R.string.labels_load_error),
                    onRetry = onRetryLoad,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(text = stringResource(R.string.a11y_loading_labels))
                    }
                }
                state.labels.isEmpty() -> EmptyState(
                    title = stringResource(R.string.empty_labels_title),
                    body = stringResource(R.string.empty_labels_body),
                    actionLabel = stringResource(R.string.label_new),
                    onAction = onCreate,
                    icon = AppIcons.Tag,
                    modifier = Modifier.fillMaxWidth(),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(items = state.labels, key = { it.id }) { label ->
                        LabelRow(
                            label = label,
                            onClick = { onLabelClick(label.id) },
                            onEdit = { onLabelClick(label.id) },
                            onDelete = { onDeleteRequest(label.id) },
                        )
                    }
                }
            }
        }
    }

    state.editor?.let { editor ->
        LabelEditorSheet(
            state = editor,
            isSaving = state.isSaving,
            onNameChange = onEditorNameChange,
            onColorSelected = onEditorColorSelected,
            onSave = onEditorSave,
            onCancel = onEditorCancel,
            maxNameLength = com.foli.listadetarefas.domain.model.TaskLimits.LABEL_NAME_MAX,
            nameFocusRequester = nameFocusRequester,
        )
    }

    if (state.pendingDelete != null) {
        DeleteLabelDialog(
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
        )
    }
}
