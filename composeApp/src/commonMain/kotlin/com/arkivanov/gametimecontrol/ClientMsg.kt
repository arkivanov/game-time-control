package com.arkivanov.gametimecontrol

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
sealed interface ClientMsg {

    @Serializable
    data class AddTime(val duration: Duration) : ClientMsg

    @Serializable
    data class SetPinCode(val pinCode: String) : ClientMsg

    @Serializable
    data class SetVoiceEnabled(val isEnabled: Boolean) : ClientMsg
}
