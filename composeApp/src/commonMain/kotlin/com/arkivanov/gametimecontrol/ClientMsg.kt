package com.arkivanov.gametimecontrol

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed interface ClientMsg {

    @Serializable
    data class AddTime(val duration: Duration) : ClientMsg
}
