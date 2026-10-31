package com.foli.listadetarefas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.foli.listadetarefas.ui.feature.editor.presentation.TaskEditorRoute
import com.foli.listadetarefas.ui.feature.labels.presentation.LabelListRoute
import com.foli.listadetarefas.ui.feature.tasks.presentation.TaskListRoute

/**
 * Host de navegação. S1 (`TaskList`) é a raiz; S2 (`TaskEditor`) é empilhada.
 * O destino do editor só é registrado na T05, mas o wiring da lista já navega
 * para ele; aqui os dois destinos convivem no estado final da história.
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.TaskList,
    ) {
        composable<AppDestination.TaskList> {
            TaskListRoute(
                onNavigateToEditor = { taskId ->
                    navController.navigate(AppDestination.TaskEditor(taskId))
                },
                onNavigateToLabels = {
                    navController.navigate(AppDestination.Labels)
                },
            )
        }
        composable<AppDestination.TaskEditor> {
            TaskEditorRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<AppDestination.Labels> {
            LabelListRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
