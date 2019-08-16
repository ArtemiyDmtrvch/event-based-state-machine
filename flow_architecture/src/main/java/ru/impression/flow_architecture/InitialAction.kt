package ru.impression.flow_architecture

/**
 * The first action performed in a [Flow] scope before any [Event] occurred. This action is intended to initialize the
 * [flow performers][FlowPerformer]. It is also cached so that [flow performers][FlowPerformer] can
 * [attach to flow][attachToFlow] with replay.
 * @see [FlowPerformer.AttachmentType.REPLAY_ATTACHMENT]
 */
sealed class InitialAction : Action()

/**
 * [InitialAction] that occurs in the scope of only one [Flow]. It is performed at the very beginning of the
 * [start method][Flow.start].
 */
abstract class UnilateralInitialAction : InitialAction()

/**
 * [InitialAction] that is called from an already created [Flow] and creates a new [Flow], becoming it's
 * [InitialAction]. Is performed in the parent and child [Flow] scopes.
 */
abstract class BilateralInitialAction(val flowClass: Class<out Flow>) : InitialAction()