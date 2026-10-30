package com.foli.listadetarefas.ui.feature.tasks.presentation

import android.content.res.Resources
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import com.foli.listadetarefas.R
import com.foli.listadetarefas.domain.model.TaskTab
import com.foli.listadetarefas.ui.UiText
import com.foli.listadetarefas.ui.designsystem.components.ActionTopAppBar
import com.foli.listadetarefas.ui.designsystem.components.EmptyState
import com.foli.listadetarefas.ui.designsystem.components.ErrorBanner
import com.foli.listadetarefas.ui.designsystem.components.TaskRow
import com.foli.listadetarefas.ui.designsystem.icons.AppIcons
import com.foli.listadetarefas.ui.feature.tasks.presentation.components.DeleteTaskDialog
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Route da S1: coleta estado/efeitos do ViewModel e cuida da navegação e do
 * snackbar de erro. A tela abaixo é stateless.
 */
@Composable
fun TaskListRoute(
    onNavigateToEditor: (String?) -> Unit,
    viewModel: TaskListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // Resolvidos na composição (respeitam locale/configuração e evitam
    // consultar recursos a partir de `LocalContext` dentro do efeito).
    val resources = LocalResources.current
    val retryLabel = stringResource(R.string.retry)

    LaunchedEffect(viewModel, resources, retryLabel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TaskListEffect.NavigateToEditor -> onNavigateToEditor(effect.taskId)
                is TaskListEffect.ShowStorageError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message.asString(resources),
                        actionLabel = retryLabel,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(TaskListAction.RetryRequested(effect.operation))
                    }
                }
            }
        }
    }

    TaskListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onTabSelected = { tab -> viewModel.onAction(TaskListAction.TabSelected(tab)) },
        onCompletionToggled = { taskId, isCompleted ->
            viewModel.onAction(TaskListAction.CompletionToggled(taskId, isCompleted))
        },
        onNewTask = { viewModel.onAction(TaskListAction.NewTaskClicked) },
        onTaskClick = { taskId -> viewModel.onAction(TaskListAction.TaskClicked(taskId)) },
        onEditTask = { taskId -> viewModel.onAction(TaskListAction.TaskClicked(taskId)) },
        onDeleteRequest = { taskId -> viewModel.onAction(TaskListAction.DeleteRequested(taskId)) },
        onDeleteConfirm = { viewModel.onAction(TaskListAction.DeleteConfirmed) },
        onDeleteDismiss = { viewModel.onAction(TaskListAction.DeleteDismissed) },
        onRetryLoad = { viewModel.onAction(TaskListAction.RetryLoadClicked) },
    )
}

@Composable
private fun TaskListScreen(
    state: TaskListState,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (TaskTab) -> Unit,
    onCompletionToggled: (String, Boolean) -> Unit,
    onNewTask: () -> Unit,
    onTaskClick: (String) -> Unit,
    onEditTask: (String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onRetryLoad: () -> Unit,
) {
    val isActiveTabEmpty = state.rows.isEmpty() &&
        !state.isLoading &&
        !state.loadFailed &&
        state.tab == TaskTab.ACTIVE
    val showFab = !isActiveTabEmpty

    Scaffold(
        topBar = {
            ActionTopAppBar(title = stringResource(R.string.task_list_title))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = onNewTask,
                    icon = {
                        Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = null,
                        )
                    },
                    text = { Text(text = stringResource(R.string.new_task)) },
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
            TaskTabs(
                selectedTab = state.tab,
                onTabSelected = onTabSelected,
            )

            if (state.loadFailed) {
                ErrorBanner(
                    message = UiText.Res(R.string.task_load_error),
                    onRetry = onRetryLoad,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(text = stringResource(R.string.a11y_loading_tasks))
                    }
                }
            } else if (state.rows.isEmpty()) {
                if (state.tab == TaskTab.ACTIVE) {
                    EmptyState(
                        title = stringResource(R.string.empty_active_title),
                        body = stringResource(R.string.empty_active_body),
                        actionLabel = stringResource(R.string.new_task),
                        onAction = onNewTask,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    EmptyState(
                        title = stringResource(R.string.empty_completed_title),
                        body = stringResource(R.string.empty_completed_body),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(items = state.rows, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onClick = { onTaskClick(task.id) },
                            onEdit = { onEditTask(task.id) },
                            onDelete = { onDeleteRequest(task.id) },
                            onToggle = { isCompleted ->
                                onCompletionToggled(task.id, isCompleted)
                            },
                        )
                    }
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        DeleteTaskDialog(
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
        )
    }
}

@Composable
private fun TaskTabs(
    selectedTab: TaskTab,
    onTabSelected: (TaskTab) -> Unit,
) {
    val activeDescription = stringResource(R.string.a11y_switch_tab_active)
    val completedDescription = stringResource(R.string.a11y_switch_tab_completed)

    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == TaskTab.ACTIVE,
            onClick = { onTabSelected(TaskTab.ACTIVE) },
            modifier = Modifier.semantics { contentDescription = activeDescription },
            text = {
                Text(
                    text = stringResource(R.string.tab_active),
                    style = MaterialTheme.typography.titleSmall,
                )
            },
        )
        Tab(
            selected = selectedTab == TaskTab.COMPLETED,
            onClick = { onTabSelected(TaskTab.COMPLETED) },
            modifier = Modifier.semantics { contentDescription = completedDescription },
            text = {
                Text(
                    text = stringResource(R.string.tab_completed),
                    style = MaterialTheme.typography.titleSmall,
                )
            },
        )
    }
}

private fun UiText.asString(resources: Resources): String = when (this) {
    is UiText.Plain -> value
    is UiText.Res -> resources.getString(resId, *args.toTypedArray())
}
