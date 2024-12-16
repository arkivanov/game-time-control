package com.arkivanov.gametimecontrol.root

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import kotlin.time.Duration

interface RootComponent {

    val model: BehaviorObservable<Model>
    val errors: Observable<String>

    fun onHostTextChanged(text: String)
    fun onConnectButtonClicked()
    fun onPinCodeChanged(text: String)
    fun onSetPinCodeButtonClicked()
    fun onMinutesTextChanged(text: String)
    fun onAddMinutesButtonClicked()
    fun onAddTimeShortcutClicked(duration: Duration)

    data class Model(
        val connectionState: ConnectionState,
        val host: String,
        val pinCode: String,
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
