package ru.impression.flow_architecture

sealed class InitialAction : Action()

abstract class UnilateralInitialAction : InitialAction()

abstract class BilateralInitialAction(val flowClass: Class<out Flow>) : InitialAction()