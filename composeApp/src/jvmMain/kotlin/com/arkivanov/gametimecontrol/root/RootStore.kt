package com.arkivanov.gametimecontrol.root

import com.arkivanov.gametimecontrol.ClientMsg
import com.arkivanov.gametimecontrol.DEFAULT_PORT
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

data class RootState(
    val addresses: List<String>,
    val currentTime: ComparableTimeMark,
    val endTime: ComparableTimeMark? = null,
)

fun RootState.remainingTime(): Duration =
    ((endTime ?: currentTime) - currentTime).coerceAtLeast(Duration.ZERO)

fun StoreFactory.rootStore(
    clock: TimeSource.WithComparableMarks,
    mainScheduler: Scheduler,
): Store<Nothing, RootState, Nothing> =
    create(
        name = "RootStore",
        initialState = RootState(addresses = getLocalAddresses(), currentTime = clock.markNow()),
        bootstrapper = SimpleBootstrapper(Unit),
        executorFactory = { RootExecutor(clock, mainScheduler) },
        reducer = { reduce(it) },
    )

private fun getLocalAddresses(): List<String> =
    runCatching { NetworkInterface.getNetworkInterfaces().asSequence() }
        .getOrDefault(emptySequence())
        .filter {
            runCatching { it.isUp && !it.isLoopback && !it.isPointToPoint }
                .getOrDefault(false)
        }
        .flatMap { it.inetAddresses.asSequence() }
        .filterNot { it.isLinkLocalAddress }
        .map { it.hostAddress }
        .toList()

private sealed interface Msg {
    data class CurrentTimeChanged(val timeMark: ComparableTimeMark) : Msg
    data class AddTime(val duration: Duration) : Msg
}

private class RootExecutor(
    private val clock: TimeSource.WithComparableMarks,
    private val mainScheduler: Scheduler,
) : ReaktiveExecutor<Nothing, Unit, RootState, Msg, Nothing>() {
    override fun executeAction(action: Unit) {
        startServer(getState = ::state, onMessage = ::onClientMsg)

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
        }
    }
}

private fun DisposableScope.startServer(
    getState: () -> RootState,
    onMessage: (ClientMsg) -> ServerMsg?,
) {
    embeddedServer(factory = Netty, port = DEFAULT_PORT) {
        install(WebSockets) {
            pingPeriod = 5.seconds
            timeout = 5.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }

        routing {
            webSocket("/") {
                launch {
                    while (true) {
                        val msg = receiveDeserialized<ClientMsg>()
                        println("Received: $msg")

                        onMessage(msg)?.also { response ->
                            println("Response: $response")
                            sendSerialized(response)
                        }
                    }
                }

                while (true) {
                    val state = getState()
                    sendSerialized(ServerMsg.State(remainingTime = state.remainingTime()))
                    delay(500.milliseconds)
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
