package com.arkivanov.gametimecontrol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe
import com.badoo.reaktive.base.setCancellable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.observableOfNever
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.subject.behavior.BehaviorObservable

fun <T, R> BehaviorObservable<T>.map(mapper: (T) -> R): BehaviorObservable<R> =
    object : BehaviorObservable<R>, Observable<R> by (this as Observable<T>).map(mapper) {
        override val value: R get() = mapper(this@map.value)
    }

@Composable
fun <T> BehaviorObservable<T>.subscribeAsState(): State<T> {
    val state = remember(this) { mutableStateOf(value) }

    DisposableEffect(this) {
        val disposable = subscribe(onNext = { state.value = it })
        onDispose(disposable::dispose)
    }

    return state
}

fun Lifecycle.startedEvents(): Observable<Boolean> =
    observable { emitter ->
        val lifecycleCallbacks =
            subscribe(
                onStart = { emitter.onNext(true) },
                onStop = { emitter.onNext(false) },
            )

        emitter.setCancellable {
            unsubscribe(lifecycleCallbacks)
        }
    }

fun <T> Observable<T>.withLifecycleStarted(
    lifecycle: Lifecycle,
): Observable<T> =
    lifecycle
        .startedEvents()
        .switchMap { if (it) this else observableOfNever() }

fun <T> BehaviorObservable<T>.withLifecycleStarted(
    lifecycle: Lifecycle,
): BehaviorObservable<T> =
    object : BehaviorObservable<T>, Observable<T> by (this as Observable<T>).withLifecycleStarted(lifecycle) {
        override val value: T get() = this@withLifecycleStarted.value
    }
