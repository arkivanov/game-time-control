package com.arkivanov.gametimecontrol.root

import com.badoo.reaktive.subject.behavior.BehaviorObservable

interface RootComponent {

    val model: BehaviorObservable<Model>

    data class Model(
        val remainingTime: String,
    )
}
