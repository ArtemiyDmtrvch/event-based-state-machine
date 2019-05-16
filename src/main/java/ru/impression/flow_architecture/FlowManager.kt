package ru.impression.flow_architecture

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.ConcurrentHashMap

internal object FlowManager {

    fun startFlowIfNeeded(flowClass: Class<out Flow>, initiatingAction: InitiatingAction? = null) {
        val flowName = flowClass.notNullName
        if (!FLOW_PERFORMER_DISPOSABLES.containsKey(flowName)) {
            FLOW_PERFORMER_DISPOSABLES[flowName] = ConcurrentHashMap()
            FLOW_DISPOSABLES[flowName] = CompositeDisposable()
            EVENT_SUBJECTS[flowName] = PublishSubject.create()
            ACTION_SUBJECTS[flowName] = ReplaySubject.createWithSize(1)
            if (initiatingAction is RestorativeInitiatingAction)
                flowClass
                    .getConstructor(RestorativeInitiatingAction::class.java)
                    .newInstance(initiatingAction)
            else flowClass.newInstance()
        }
    }

    fun stopFlowIfNeeded(flowClass: Class<out Flow>) {
        val flowName = flowClass.notNullName
        if (FLOW_PERFORMER_DISPOSABLES[flowName]?.isEmpty() != false) {
            FLOW_PERFORMER_DISPOSABLES.remove(flowName)
            FLOW_DISPOSABLES.remove(flowName)?.dispose()
            EVENT_SUBJECTS.remove(flowName)
            ACTION_SUBJECTS.remove(flowName)
        }
    }
}