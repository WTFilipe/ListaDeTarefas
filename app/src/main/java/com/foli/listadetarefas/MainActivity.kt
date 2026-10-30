package com.foli.listadetarefas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.foli.listadetarefas.core.time.TimeSignals
import com.foli.listadetarefas.ui.navigation.AppNavHost
import com.foli.listadetarefas.ui.theme.ListaDeTarefasTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Host do app: aplica o tema Material 3, delega a navegação ao `AppNavHost` e
 * alimenta [TimeSignals] para o recálculo do prazo sem timer (US-05 AC 5):
 * ao retomar do segundo plano e ao receber mudanças de data, hora ou fuso.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var timeSignals: TimeSignals

    private val timeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            timeSignals.emit()
        }
    }

    private val timeChangeFilter = IntentFilter().apply {
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    timeSignals.emit()
                }
            },
        )
        setContent {
            ListaDeTarefasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timeChangeReceiver, timeChangeFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(timeChangeReceiver, timeChangeFilter)
        }
    }

    override fun onStop() {
        unregisterReceiver(timeChangeReceiver)
        super.onStop()
    }
}
