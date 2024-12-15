package com.arkivanov.gametimecontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.gametimecontrol.root.DefaultRootComponent
import com.arkivanov.gametimecontrol.theme.AppTheme
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.badoo.reaktive.scheduler.mainScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val rootComponent =
            DefaultRootComponent(
                componentContext = defaultComponentContext(),
                storeFactory = DefaultStoreFactory(),
                webSocketClient = webSocketsClient(),
                applicationContext = applicationContext,
                mainScheduler = mainScheduler,
            )

        setContent {
            AppTheme(modifier = Modifier.fillMaxSize()) {

            }
        }
    }
}
