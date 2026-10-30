package com.foli.listadetarefas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.foli.listadetarefas.ui.navigation.AppNavHost
import com.foli.listadetarefas.ui.theme.ListaDeTarefasTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Host do app: aplica o tema Material 3 e delega a navegação ao `AppNavHost`.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListaDeTarefasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}
