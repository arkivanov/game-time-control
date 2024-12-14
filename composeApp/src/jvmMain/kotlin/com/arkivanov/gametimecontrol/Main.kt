package com.arkivanov.gametimecontrol

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.gametimecontrol.theme.AppTheme
import java.awt.Dimension

fun main() {
    application {
        Window(
            title = "GameTimeControl",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)

            AppTheme(modifier = Modifier.fillMaxSize()) {
            }
        }
    }
}
