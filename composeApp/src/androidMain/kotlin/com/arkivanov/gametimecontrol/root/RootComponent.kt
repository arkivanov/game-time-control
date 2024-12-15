package com.arkivanov.gametimecontrol.root

import com.badoo.reaktive.subject.behavior.BehaviorObservable
import kotlin.time.Duration

interface RootComponent {

    val model: BehaviorObservable<Model>

    fun onHostTextChanged(text: String)
    fun onConnectButtonClicked()
    fun onMinutesTextChanged(text: String)
    fun onAddMinutesButtonClicked()
    fun onAddTimeShortcutClicked(duration: Duration)

    data class Model(
        val connectionState: ConnectionState,
        val host: String,
        val minutes: String,
        val remainingTime: String,
        val addTimeShortcuts: List<Duration>,
    )

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }
}
