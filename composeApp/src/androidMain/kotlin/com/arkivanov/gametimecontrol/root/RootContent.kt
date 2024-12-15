package com.arkivanov.gametimecontrol.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.gametimecontrol.root.RootComponent.ConnectionState
import com.arkivanov.gametimecontrol.subscribeAsState

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = model.connectionState.asString(),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = model.remainingTime,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = model.host,
            onValueChange = component::onHostTextChanged,
            modifier = Modifier.fillMaxWidth(fraction = 0.8F),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    text = "Host address",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = component::onConnectButtonClicked) {
            Text(text = "Connect")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = model.minutes,
            onValueChange = component::onMinutesTextChanged,
            modifier = Modifier.fillMaxWidth(fraction = 0.8F),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    text = "Minutes to add",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = component::onAddMinutesButtonClicked) {
            Text(text = "Add Minutes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        model.addTimeShortcuts.forEach { duration ->
            Button(onClick = { component.onAddTimeShortcutClicked(duration = duration) }) {
                Text(text = "Add ${duration.inWholeMinutes} Minutes")
            }
        }
    }
}

private fun ConnectionState.asString(): String =
    when (this) {
        ConnectionState.DISCONNECTED -> "Disconnected"
        ConnectionState.CONNECTING -> "Connecting..."
        ConnectionState.CONNECTED -> "Connected"
    }
