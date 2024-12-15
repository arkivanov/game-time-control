package com.arkivanov.gametimecontrol

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.gametimecontrol.root.DefaultRootComponent
import com.arkivanov.gametimecontrol.root.RootContent
import com.arkivanov.gametimecontrol.theme.AppTheme
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.badoo.reaktive.scheduler.mainScheduler
import java.awt.Dimension
import kotlin.time.TimeSource


fun main() {
    val lifecycle = LifecycleRegistry()

    val rootComponent =
        runOnUiThread {
            DefaultRootComponent(
                componentContext = DefaultComponentContext(lifecycle),
                storeFactory = DefaultStoreFactory(),
                clock = TimeSource.Monotonic,
                mainScheduler = mainScheduler,
            )
        }

    application {
        val windowState = rememberWindowState(width = 800.dp, height = 600.dp)
        val trayState = rememberTrayState()

        Tray(
            icon = rememberVectorPainter(Icons.Default.Lock),
            state = trayState,
            onAction = { windowState.isMinimized = false },
            menu = {
                Item(
                    text = "Game Time Control",
                    onClick = { windowState.isMinimized = false },
                )
            },
        )

        Window(
            title = "GameTimeControl",
            state = windowState,
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)

            AppTheme(modifier = Modifier.fillMaxSize()) {
                RootContent(
                    component = rootComponent,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current,
            )
        }
    }
}
