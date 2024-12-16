package com.arkivanov.gametimecontrol.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CloseDialog(
    correctPinCode: () -> String,
    onDismissRequest: () -> Unit,
    onCloseRequest: () -> Unit,
) {
    var pinCode by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Enter Pin Code",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))


                TextField(
                    value = pinCode,
                    onValueChange = { pinCode = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    placeholder = {
                        Text(
                            text = "Pin code",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                    visualTransformation = { text ->
                        TransformedText(
                            text = AnnotatedString("*".repeat(text.text.length)),
                            offsetMapping = OffsetMapping.Identity,
                        )
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = onDismissRequest) {
                        Text(text = "Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (pinCode == correctPinCode()) {
                                onCloseRequest()
                            } else {
                                pinCode = ""
                            }
                        },
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}
