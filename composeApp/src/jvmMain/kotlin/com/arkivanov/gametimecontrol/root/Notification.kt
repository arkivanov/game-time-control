package com.arkivanov.gametimecontrol.root

sealed interface Notification {

    val type: NotificationType
    val isReadable: Boolean

    data class MinutesRemaining(
        override val type: NotificationType,
        override val isReadable: Boolean,
        val minutes: Int,
    ) : Notification

    data class Message(
        override val isReadable: Boolean,
        val message: String,
    ) : Notification {
        override val type: NotificationType = NotificationType.Info
    }
}

typealias NotificationType = androidx.compose.ui.window.Notification.Type
