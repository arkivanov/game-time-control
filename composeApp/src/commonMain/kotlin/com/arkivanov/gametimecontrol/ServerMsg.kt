package com.arkivanov.gametimecontrol

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed interface ServerMsg {

    @Serializable
    data class State(
        val remainingTime: Duration,
        val isVoiceEnabled: Boolean,
    ) : ServerMsg
}
