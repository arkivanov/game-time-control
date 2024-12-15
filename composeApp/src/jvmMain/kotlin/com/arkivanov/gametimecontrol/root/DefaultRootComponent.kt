package com.arkivanov.gametimecontrol.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.reaktive.disposableScope
import com.arkivanov.gametimecontrol.formatTime
import com.arkivanov.gametimecontrol.map
import com.arkivanov.gametimecontrol.root.RootComponent.Model
import com.arkivanov.gametimecontrol.withLifecycleStarted
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.labels
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.badoo.reaktive.disposable.scope.DisposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import kotlin.time.TimeSource

class DefaultRootComponent(
    private val componentContext: ComponentContext,
    storeFactory: StoreFactory,
    clock: TimeSource.WithComparableMarks,
    mainScheduler: Scheduler,
    shutdownSignal: () -> Unit,
) : RootComponent, ComponentContext by componentContext, DisposableScope by componentContext.disposableScope() {

    private val store = storeFactory.rootStore(clock, mainScheduler)

    override val model: BehaviorObservable<Model> =
        store.states
            .map { it.toModel() }
            .withLifecycleStarted(lifecycle)

    override val notifications: Observable<String> =
        store.labels.mapNotNull {
            when (it) {
                RootLabel.TenMinutesRemaining -> "10 MINUTES LEFT!"
                RootLabel.OneMinuteRemaining -> "1 MINUTE LEFT!"
                RootLabel.TimeOut -> null
            }
        }

    init {
        store.labels
            .mapNotNull { it as? RootLabel.TimeOut }
            .subscribeScoped { shutdownSignal() }
    }

    private fun RootState.toModel(): Model =
        Model(
            addresses = addresses.ifEmpty { listOf("Unknown address") },
            remainingTime = remainingTime().formatTime(),
        )
}
