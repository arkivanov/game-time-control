package com.arkivanov.gametimecontrol.root

sealed interface Notification {

    val type: NotificationType

    data class MinutesRemaining(
        override val type: NotificationType,
        val minutes: Int,
    ) : Notification
}

typealias NotificationType = androidx.compose.ui.window.Notification.Type
