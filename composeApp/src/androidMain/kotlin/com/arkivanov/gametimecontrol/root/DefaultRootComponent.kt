package com.arkivanov.gametimecontrol.root

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.gametimecontrol.formatTime
import com.arkivanov.gametimecontrol.map
import com.arkivanov.gametimecontrol.root.RootComponent.ConnectionState
import com.arkivanov.gametimecontrol.root.RootComponent.Model
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.states
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

    override fun onHostTextChanged(text: String) {
        store.accept(RootIntent.SetHost(host = text))
    }

    override fun onMinutesTextChanged(text: String) {
        store.accept(RootIntent.SetMinutes(minutes = text))
    }

    override fun onAddMinutesButtonClicked() {
        store.accept(RootIntent.AddMinutes)
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
            minutes = minutes,
            remainingTime = remainingTime?.formatTime() ?: "0:00:00",
            addTimeShortcuts = ADD_TIME_SHORTCUTS,
        )

    private companion object {
        private val ADD_TIME_SHORTCUTS =
            listOf(
                5.minutes,
                10.minutes,
                15.minutes,
                20.minutes,
            )
    }
}
