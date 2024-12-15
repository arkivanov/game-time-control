package com.arkivanov.gametimecontrol

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Notification
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
import com.arkivanov.mvikotlin.core.utils.setMainThreadId
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.badoo.reaktive.coroutinesinterop.asScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.scheduler.overrideSchedulers
import kotlinx.coroutines.Dispatchers
import kotlin.system.exitProcess
import kotlin.time.TimeSource


fun main() {
    overrideSchedulers(main = { Dispatchers.Main.asScheduler() })

    val lifecycle = LifecycleRegistry()

    val rootComponent =
        runOnUiThread {
            setMainThreadId(Thread.currentThread().id)

            DefaultRootComponent(
                componentContext = DefaultComponentContext(lifecycle),
                storeFactory = DefaultStoreFactory(),
                clock = TimeSource.Monotonic,
                mainScheduler = mainScheduler,
                shutdownSignal = {
                    Runtime.getRuntime().exec("shutdown -s -t 0")
                    exitProcess(0)
                },
            )
        }

    application {
        val windowState = rememberWindowState(width = 400.dp, height = 300.dp, isMinimized = true)
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

        ObservableEffect(rootComponent.notifications) { message ->
            trayState.sendNotification(
                Notification(
                    title = "Game Time Control",
                    message = message,
                    type = Notification.Type.Warning,
                )
            )
        }

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "GameTimeControl",
            resizable = false,
        ) {
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
