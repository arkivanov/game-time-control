package com.arkivanov.gametimecontrol

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed interface ServerMsg {

    data class State(val remainingTime: Duration) : ServerMsg
}
