package ru.impression.flow_architecture


/**
 * Since [Flow] has one method ([Flow.start]) with which all its logic begins, the implementation of multiple
 * interfaces can be used to extend this method. SubFlow is such an interface. It can contain a description of business
 * logic that can be reused across multiple Flows. Create an interface that inherits from SubFlow, override
 * [SubFlow.start] method inside which cast your SubFlow to Flow and describe the business logic as in Flow.
 * <pre>
 * `interface MyAwesomeSubPageFlow : SubFlow {
 *
 *      override fun start() {
 *           this as Flow
 *
 *           whenEventOccurs<MyAwesomeDataRequested> { performAction(LoadMyAwesomeData()) }
 *
 *           whenEventOccurs<MyAwesomeDataLoaded> { performAction(ShowMyAwesomeData(it.data)) }
 *      }
 * }
 *
 * class MyAwesomeDataRequested : Event()
 * class LoadMyAwesomeData : Action()
 *
 * class DataMyAwesomeLoaded(val data: Array<String>) : Event()
 * class ShowMyAwesomeData(val data: Array<String>) : Action()`
 * </pre>
 * Later in Flow, just implement your interface and call `super.start()`.
 * @see SubFlowPerformer
 */
interface SubFlow {

    fun start()
}