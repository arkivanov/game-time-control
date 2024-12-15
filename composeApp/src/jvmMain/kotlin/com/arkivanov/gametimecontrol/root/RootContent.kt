package com.arkivanov.gametimecontrol.root

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    ) {
        model.addresses.forEach { address ->
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().weight(1F),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = model.remainingTime,
                style = MaterialTheme.typography.displayLarge,
            )
        }
    }
}

@Preview
@Composable
fun RootContentPreview() {
    RootContent(
        component = PreviewRootComponent(
            model = RootComponent.Model(
                addresses = listOf("192.168.0.1", "192.168.0.2"),
                remainingTime = "2:32:17",
            )
        ),
    )
}
