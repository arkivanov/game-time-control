package com.arkivanov.gametimecontrol.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.gametimecontrol.ObservableEffect
import com.arkivanov.gametimecontrol.root.RootComponent.ConnectionState
import com.arkivanov.gametimecontrol.subscribeAsState
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObservableEffect(component.errors) {
        scope.launch {
            snackbarHostState.showSnackbar(message = it)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Game Time Control")
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            ConnectionControls(
                connectionState = model.connectionState,
                remainingTime = model.remainingTime,
                host = model.host,
                onHostTextChanged = component::onHostTextChanged,
                onConnectButtonClick = component::onConnectButtonClicked,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            TimeControls(
                minutes = model.minutes,
                onMinutesTextChanged = component::onMinutesTextChanged,
                onAddMinutesButtonClick = component::onAddMinutesButtonClicked,
                addTimeShortcuts = model.addTimeShortcuts,
                onAddTimeShortcutClicked = component::onAddTimeShortcutClicked,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Settings(
                pinCode = model.pinCode,
                onPinCodeChanged = component::onPinCodeChanged,
                onSetPinCodeButtonClicked = component::onSetPinCodeButtonClicked,
                isVoiceEnabled = model.isVoiceEnabled,
                onVoiceEnabledChanged = component::onVoiceEnabledChanged,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConnectionControls(
    connectionState: ConnectionState,
    remainingTime: String,
    host: String,
    onHostTextChanged: (String) -> Unit,
    onConnectButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = connectionState.asString(),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = remainingTime,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = host,
            onValueChange = onHostTextChanged,
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

        Button(onClick = onConnectButtonClick) {
            Text(text = "Connect")
        }
    }
}

private fun ConnectionState.asString(): String =
    when (this) {
        ConnectionState.DISCONNECTED -> "Disconnected"
        ConnectionState.CONNECTING -> "Connecting..."
        ConnectionState.CONNECTED -> "Connected"
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeControls(
    minutes: String,
    onMinutesTextChanged: (String) -> Unit,
    onAddMinutesButtonClick: () -> Unit,
    addTimeShortcuts: List<Duration>,
    onAddTimeShortcutClicked: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = minutes,
            onValueChange = onMinutesTextChanged,
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

        Button(onClick = onAddMinutesButtonClick) {
            Text(text = "Add Minutes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
        ) {
            addTimeShortcuts.forEach { duration ->
                Button(onClick = { onAddTimeShortcutClicked(duration) }) {
                    Text(text = "+${duration.inWholeMinutes} min")
                }
            }
        }
    }
}

@Composable
private fun Settings(
    pinCode: String,
    onPinCodeChanged: (String) -> Unit,
    onSetPinCodeButtonClicked: () -> Unit,
    isVoiceEnabled: Boolean,
    onVoiceEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = pinCode,
            onValueChange = onPinCodeChanged,
            modifier = Modifier.fillMaxWidth(fraction = 0.8F),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    text = "PIN Code",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onSetPinCodeButtonClicked) {
            Text(text = "Set PIN Code")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.clickable { onVoiceEnabledChanged(!isVoiceEnabled) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = isVoiceEnabled, onCheckedChange = onVoiceEnabledChanged)

            Text(
                text = "Enable voice",
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}
