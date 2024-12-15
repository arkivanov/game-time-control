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
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.map
import com.badoo.reaktive.single.observeOn
import com.badoo.reaktive.single.singleFromFunction
import com.badoo.reaktive.single.singleOf
import com.badoo.reaktive.single.subscribe
import com.badoo.reaktive.single.subscribeOn
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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
        startServer(
            getState = { singleFromFunction(::state).subscribeOn(mainScheduler) },
            onMessage = { singleOf(it).observeOn(mainScheduler).map(::onClientMsg) },
        )

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

suspend fun <T> Single<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        val disposable = subscribe(onError = continuation::resumeWithException, onSuccess = continuation::resume)
        continuation.invokeOnCancellation { disposable.dispose() }
    }

private fun DisposableScope.startServer(
    getState: () -> Single<RootState>,
    onMessage: (ClientMsg) -> Single<ServerMsg?>,
) {
    embeddedServer(factory = Netty, port = DEFAULT_PORT) {
        install(WebSockets) {
            pingPeriod = 5.seconds
            timeout = 5.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }

        routing {
            webSocket("/") {
                println("Client connected")

                try {
                    launch {
                        while (true) {
                            val state = getState().await()
                            sendSerialized<ServerMsg>(ServerMsg.State(remainingTime = state.remainingTime()))
                            delay(250.milliseconds)
                        }
                    }

                    while (true) {
                        val msg = receiveDeserialized<ClientMsg>()
                        println("Received: $msg")

                        val response = onMessage(msg).await()
                        if (response != null) {
                            println("Response: $response")
                            sendSerialized<ServerMsg>(response)
                        }
                    }
                } finally {
                    println("Client disconnected")
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
