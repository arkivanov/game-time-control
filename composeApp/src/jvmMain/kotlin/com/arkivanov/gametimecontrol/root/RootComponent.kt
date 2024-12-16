package com.arkivanov.gametimecontrol.root

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.behavior.BehaviorObservable

interface RootComponent {

    val model: BehaviorObservable<Model>
    val notifications: Observable<String>

    data class Model(
        val addresses: List<String>,
        val pinCode: String,
        val remainingTime: String,
    )
}
