package com.arkivanov.gametimecontrol

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.gametimecontrol.theme.AppTheme
import java.awt.Dimension


fun main() {
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
            }
        }
    }
}
