package com.arkivanov.gametimecontrol.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.gametimecontrol.formatTime
import com.arkivanov.gametimecontrol.map
import com.arkivanov.gametimecontrol.root.RootComponent.Model
import com.arkivanov.gametimecontrol.withLifecycleStarted
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import kotlin.time.TimeSource

class DefaultRootComponent(
    private val componentContext: ComponentContext,
    storeFactory: StoreFactory,
    clock: TimeSource.WithComparableMarks,
    mainScheduler: Scheduler,
) : RootComponent, ComponentContext by componentContext {

    private val store = storeFactory.rootStore(clock, mainScheduler)

    override val model: BehaviorObservable<Model> =
        store.states
            .map { it.toModel() }
            .withLifecycleStarted(lifecycle)

    private fun RootState.toModel(): Model =
        Model(
            addresses = addresses.ifEmpty { listOf("Unknown address") },
            remainingTime = remainingTime().formatTime(),
        )
}
