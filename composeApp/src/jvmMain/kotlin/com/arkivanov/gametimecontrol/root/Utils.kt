package com.arkivanov.gametimecontrol.root

import kotlin.time.Duration

fun Duration.formatTime(): String =
    toComponents { hours, minutes, seconds, _ ->
        "$hours:${minutes.toString().padZero()}:${seconds.toString().padZero()}"
    }

private fun String.padZero(): String =
    padStart(length = 2, padChar = '0')
