package ru.impression.flow_machine

import io.reactivex.disposables.CompositeDisposable

interface FlowInitiator<F : Flow<*>> {

    val flowClass: Class<F>

    fun startFlow(holdLastEventsCount: Int = 0, holdLastActionsCount: Int = 0) {
        flowClass.canonicalName?.let { flowName ->
            DISPOSABLES[flowName]?.let { return }
            val flowInstance = flowClass.newInstance()
            DISPOSABLES[flowName] = CompositeDisposable()
            EVENT_SUBJECTS[flowName] = createSubjectForBufferedItemsCount(holdLastEventsCount)
            ACTION_SUBJECTS[flowName] = createSubjectForBufferedItemsCount(holdLastEventsCount)
            flowInstance.start()
        }
    }

    fun finishFlow() {
        flowClass.canonicalName?.let { flowName ->
            DISPOSABLES[flowName]?.dispose()
            DISPOSABLES.remove(flowName)
            EVENT_SUBJECTS.remove(flowName)
            ACTION_SUBJECTS.remove(flowName)
        }
    }
}