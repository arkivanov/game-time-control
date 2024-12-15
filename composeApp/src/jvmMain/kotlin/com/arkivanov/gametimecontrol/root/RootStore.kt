package com.arkivanov.gametimecontrol.root

import com.arkivanov.gametimecontrol.ClientMsg
import com.arkivanov.gametimecontrol.ServerMsg
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.observableInterval
import com.badoo.reaktive.scheduler.Scheduler
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlinx.serialization.json.Json
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

data class RootState(
    val currentTime: ComparableTimeMark,
    val endTime: ComparableTimeMark? = null,
)

fun StoreFactory.rootStore(
    clock: TimeSource.WithComparableMarks,
    mainScheduler: Scheduler,
): Store<Nothing, RootState, Nothing> =
    create(
        name = "RootStore",
        initialState = RootState(currentTime = clock.markNow()),
        bootstrapper = SimpleBootstrapper(Unit),
        executorFactory = { RootExecutor(clock, mainScheduler) },
        reducer = { reduce(it) },
    )

private sealed interface Msg {
    data class CurrentTimeChanged(val timeMark: ComparableTimeMark) : Msg
    data class AddTime(val duration: Duration) : Msg
}

private class RootExecutor(
    private val clock: TimeSource.WithComparableMarks,
    private val mainScheduler: Scheduler,
) : ReaktiveExecutor<Nothing, Unit, RootState, Msg, Nothing>() {
    override fun executeAction(action: Unit) {
        startServer(onMessage = ::onClientMsg)

        observableInterval(period = 250.milliseconds, scheduler = mainScheduler).subscribeScoped {
            dispatch(Msg.CurrentTimeChanged(clock.markNow()))
        }
    }

    private fun onClientMsg(msg: ClientMsg): ServerMsg? {
        when (msg) {
            is ClientMsg.AddTime -> {
                dispatch(Msg.AddTime(duration = msg.duration))
                return null
            }

            is ClientMsg.GetState -> {
                val state = state()
                val endTime = state.endTime ?: state.currentTime
                val remainingTime = (endTime - state.currentTime).coerceAtLeast(Duration.ZERO)
                return ServerMsg.State(remainingTime = remainingTime)
            }
        }
    }
}

private fun DisposableScope.startServer(
    onMessage: (ClientMsg) -> ServerMsg?,
) {
    embeddedServer(factory = Netty, port = 9876) {
        install(WebSockets) {
            pingPeriod = 5.seconds
            timeout = 5.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }

        routing {
            webSocket("/") {
                while (true) {
                    val msg = receiveDeserialized<ClientMsg>()
                    println("Received: $msg")

                    onMessage(msg)?.also { response ->
                        println("Response: $response")
                        sendSerialized(response)
                    }
                }
            }
        }
    }
        .start()
        .scope { it.stop() }
}

private fun RootState.reduce(msg: Msg): RootState =
    when (msg) {
        is Msg.CurrentTimeChanged -> copy(currentTime = msg.timeMark)
        is Msg.AddTime -> copy(endTime = (endTime ?: currentTime) + msg.duration)
    }
