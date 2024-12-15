package com.arkivanov.gametimecontrol.root

import kotlin.time.Duration

fun Duration.formatTime(): String =
    toComponents { hours, minutes, seconds, _ -> "$hours:$minutes:$seconds" }
