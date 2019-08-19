package ru.impression.flow_architecture

/**
 * Since [FlowPerformer] has one method ([FlowPerformer.performAction]) with which all its logic begins, the
 * implementation of multiple interfaces can be used to extend this method. SubFlowPerformer is such an interface. As
 * with [SubFlow], create an interface that inherits from SubFlowPerformer, override [SubFlowPerformer.performAction]
 * method inside which cast your SubFlowPerformer to FlowPerformer that you want to expand in the future and add your
 * logic.
 * <pre>
 * `interface MyAwesomeSubFlowPerformer : SubFlowPerformer {
 *
 *      override fun performAction(action: Action) {
 *          this as MyAwesomeFlowPerformer
 *
 *          when (action) {
 *              is LoadMyAwesomeData -> backend.loadData { eventOccurred(MyAwesomeDataLoaded(it)) }
 *          }
 *      }
 *  }`
 * </pre>
 * Later in FlowPerformer, just implement your interface and call `super.performAction(action)`.
 */
interface SubFlowPerformer {

    fun performAction(action: Action)
}