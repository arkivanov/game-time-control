package com.arkivanov.gametimecontrol.root

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.subscribe
import com.arkivanov.gametimecontrol.formatTime
import com.arkivanov.gametimecontrol.map
import com.arkivanov.gametimecontrol.root.RootComponent.ConnectionState
import com.arkivanov.gametimecontrol.root.RootComponent.Model
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.labels
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import io.ktor.client.HttpClient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class DefaultRootComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    webSocketClient: HttpClient,
    applicationContext: Context,
    mainScheduler: Scheduler,
) : RootComponent, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore(key = "RootStore") {
            storeFactory.rootStore(
                webSocketsClient = webSocketClient,
                settings = DefaultRootSettings(applicationContext),
                mainScheduler = mainScheduler,
            )
        }

    override val model: BehaviorObservable<Model> = store.states.map { it.toModel() }
    override val errors: Observable<String> = store.labels.mapNotNull { it as? RootLabel.Error }.map { it.message }

    init {
        lifecycle.subscribe(
            onStart = { store.accept(RootIntent.Connect) },
            onStop = { store.accept(RootIntent.Disconnect) },
        )
    }

    override fun onHostTextChanged(text: String) {
        store.accept(RootIntent.SetHost(host = text))
    }

    override fun onConnectButtonClicked() {
        store.accept(RootIntent.Connect)
    }

    override fun onPinCodeChanged(text: String) {
        store.accept(RootIntent.SetPinCode(pinCode = text))
    }

    override fun onSetPinCodeButtonClicked() {
        store.accept(RootIntent.SavePinCode)
    }

    override fun onVoiceEnabledChanged(isEnabled: Boolean) {
        store.accept(RootIntent.SetVoiceEnabled(isEnabled = isEnabled))
    }

    override fun onMinutesTextChanged(text: String) {
        store.accept(RootIntent.SetMinutes(minutes = text))
    }

    override fun onAddMinutesButtonClicked() {
        store.accept(RootIntent.AddMinutes)
    }

    override fun onMessageTextChanged(text: String) {
        store.accept(RootIntent.SetMessage(text))
    }

    override fun onSendMessageButtonClicked() {
        store.accept(RootIntent.SendMessage)
    }

    override fun onAddTimeShortcutClicked(duration: Duration) {
        store.accept(RootIntent.AddTime(duration = duration))
    }

    private fun RootState.toModel(): Model =
        Model(
            connectionState = when (connection) {
                is Connection.Disconnected -> ConnectionState.DISCONNECTED
                is Connection.Connecting -> ConnectionState.CONNECTING
                is Connection.Connected -> ConnectionState.CONNECTED
            },
            host = host,
            pinCode = pinCode,
            isVoiceEnabled = isVoiceEnabled,
            minutes = minutes,
            message = message,
            remainingTime = remainingTime?.formatTime() ?: "0:00:00",
            addTimeShortcuts = ADD_TIME_SHORTCUTS,
        )

    private companion object {
        private val ADD_TIME_SHORTCUTS =
            listOf(
                1.minutes,
                5.minutes,
                10.minutes,
                15.minutes,
                30.minutes,
            )
    }
}
