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
    fun onVoiceEnabledChanged(isEnabled: Boolean)
    fun onMinutesTextChanged(text: String)
    fun onAddMinutesButtonClicked()
    fun onMessageTextChanged(text: String)
    fun onSendMessageButtonClicked()
    fun onAddTimeShortcutClicked(duration: Duration)

    data class Model(
        val connectionState: ConnectionState,
        val host: String,
        val pinCode: String,
        val isVoiceEnabled: Boolean,
        val minutes: String,
        val message: String,
        val remainingTime: String,
        val addTimeShortcuts: List<Duration>,
    )

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }
}
