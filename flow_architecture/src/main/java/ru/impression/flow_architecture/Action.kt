package ru.impression.flow_architecture

/**
 * A component of the business logic of an application, implying some kind of process that has a length and is performed
 * by someone. Actions are emitted by [Flow] and delivered to [all its performers][FlowPerformer]. Create in your Action
 * fields with data that you want to transfer from [Flow] to [FlowPerformer].
 *
 * NOTE that Action names should be chosen from a business point of view, they should not be tied to technical details,
 * for example, to UI elements. If the action involves going to the server for data and displaying a loading indicator,
 * simply name it "LoadData".
 */
abstract class Action