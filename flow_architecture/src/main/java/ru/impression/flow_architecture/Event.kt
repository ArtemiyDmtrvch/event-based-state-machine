package ru.impression.flow_architecture

/**
 * A component of the business logic of an application, implying something that happened (some completed [Action]) and a
 * starting point for a new action. Event occurs in [FlowPerformer] and is passed to [Flow].
 *
 * NOTE that Event names should be chosen from a business point of view, they should not be tied to technical details,
 * for example, to UI elements. If an event occurs when a button is pressed, name it not “SomeButtonPressed”, but, for
 * example, “AuthorizationRequested” or “ProductDataRequested”.
 */
abstract class Event