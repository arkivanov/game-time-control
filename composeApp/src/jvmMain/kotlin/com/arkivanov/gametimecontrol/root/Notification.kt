package com.arkivanov.gametimecontrol.root

sealed interface Notification {

    val type: NotificationType
    val isReadable: Boolean

    data class MinutesRemaining(
        override val type: NotificationType,
        override val isReadable: Boolean,
        val minutes: Int,
    ) : Notification
}

typealias NotificationType = androidx.compose.ui.window.Notification.Type
