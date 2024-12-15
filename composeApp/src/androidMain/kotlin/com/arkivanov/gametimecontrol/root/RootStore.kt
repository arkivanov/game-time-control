package com.arkivanov.gametimecontrol.root

import com.arkivanov.gametimecontrol.ClientMsg
import com.arkivanov.gametimecontrol.ServerMsg
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.badoo.reaktive.coroutinesinterop.completableFromCoroutine
import com.badoo.reaktive.coroutinesinterop.singleFromCoroutine
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.single.doOnBeforeSuccess
import com.badoo.reaktive.single.flatMapObservable
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.repeat
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.DEFAULT_PORT
import kotlinx.coroutines.cancel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class RootState(
    val host: String,
    val connection: Connection = Connection.Disconnected,
    val remainingTime: Duration? = null,
    val minutes: String = "0",
)

sealed interface Connection {
    data object Disconnected : Connection
    data object Connecting : Connection
    data class Connected(val session: DefaultClientWebSocketSession) : Connection
}

sealed interface RootIntent {
    data object Connect : RootIntent
    data object Disconnect : RootIntent
    data class SetMinutes(val minutes: String) : RootIntent
    data object AddMinutes : RootIntent
    data class AddTime(val duration: Duration) : RootIntent
    data class SetHost(val host: String) : RootIntent
}

fun StoreFactory.rootStore(
    webSocketsClient: HttpClient,
    settings: RootSettings,
    mainScheduler: Scheduler,
): Store<RootIntent, RootState, Nothing> =
    create(
        name = "RootStore",
        initialState = RootState(host = settings.host ?: "192.168.0.1"),
        executorFactory = {
            RootExecutor(
                webSocketsClient = webSocketsClient,
                settings = settings,
                mainScheduler = mainScheduler,
            )
        },
        reducer = { reduce(it) },
    )

private sealed interface Msg {
    data class SetConnection(val connection: Connection) : Msg
    data class ApplyServerState(val state: ServerMsg.State) : Msg
    data class SetMinutes(val minutes: String) : Msg
    data class SetHost(val host: String) : Msg
}

private class RootExecutor(
    private val webSocketsClient: HttpClient,
    private val settings: RootSettings,
    private val mainScheduler: Scheduler,
) : ReaktiveExecutor<RootIntent, Nothing, RootState, Msg, Nothing>() {

    override fun executeIntent(intent: RootIntent) {
        when (intent) {
            is RootIntent.Connect -> connect()
            is RootIntent.Disconnect -> disconnect()
            is RootIntent.SetMinutes -> dispatch(Msg.SetMinutes(minutes = intent.minutes))
            is RootIntent.AddMinutes -> addMinutes()
            is RootIntent.AddTime -> sendMessage(ClientMsg.AddTime(duration = intent.duration))
            is RootIntent.SetHost -> setHost(host = intent.host)
        }
    }

    private fun connect() {
        if (state().connection !is Connection.Disconnected) {
            return
        }

        dispatch(Msg.SetConnection(Connection.Connecting))

        singleFromCoroutine {
            webSocketsClient.webSocketSession(
                host = state().host,
                port = DEFAULT_PORT,
                path = "/",
            )
        }
            .observeOn(mainScheduler)
            .doOnBeforeSuccess { dispatch(Msg.SetConnection(Connection.Connected(it))) }
            .flatMapObservable { session ->
                singleFromCoroutine { session.receiveDeserialized<ServerMsg>() }
                    .repeat()
                    .observeOn(mainScheduler)
            }
            .subscribeScoped(
                onError = { dispatch(Msg.SetConnection(Connection.Disconnected)) },
                onComplete = { dispatch(Msg.SetConnection(Connection.Disconnected)) },
                onNext = ::onServerMsg,
            )
    }

    private fun onServerMsg(msg: ServerMsg) {
        when (msg) {
            is ServerMsg.State -> dispatch(Msg.ApplyServerState(msg))
        }
    }

    private fun disconnect() {
        val session = session() ?: return
        dispatch(Msg.SetConnection(Connection.Disconnected))
        session.cancel()
    }

    private fun addMinutes() {
        val minutes = state().minutes.toIntOrNull()?.minutes ?: return
        sendMessage(ClientMsg.AddTime(duration = minutes))
    }

    private fun sendMessage(message: ClientMsg) {
        completableFromCoroutine {
            session()?.sendSerialized(message)
        }.subscribeScoped()
    }

    private fun session(): DefaultClientWebSocketSession? =
        (state().connection as? Connection.Connected)?.session

    private fun setHost(host: String) {
        settings.host = host
        dispatch(Msg.SetHost(host = host))
    }
}

private fun RootState.reduce(msg: Msg): RootState =
    when (msg) {
        is Msg.SetConnection -> copy(connection = connection)
        is Msg.ApplyServerState -> copy(remainingTime = msg.state.remainingTime)
        is Msg.SetMinutes -> copy(minutes = msg.minutes)
        is Msg.SetHost -> copy(host = msg.host)
    }
