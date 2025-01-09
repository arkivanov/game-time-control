package com.arkivanov.gametimecontrol.root

import com.arkivanov.gametimecontrol.root.RootComponent.Model
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOfNever
import com.badoo.reaktive.subject.behavior.BehaviorObservable
import com.badoo.reaktive.subject.behavior.BehaviorSubject

class PreviewRootComponent(
    model: Model,
) : RootComponent {

    override val model: BehaviorObservable<Model> = BehaviorSubject(model)
    override val notifications: Observable<Notification> = observableOfNever()
}
