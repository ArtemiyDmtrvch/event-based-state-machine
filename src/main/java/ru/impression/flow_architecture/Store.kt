package ru.impression.flow_architecture

import java.util.concurrent.CopyOnWriteArrayList

internal val FLOWS = CopyOnWriteArrayList<Flow>()

internal val WAITING_FLOWS = CopyOnWriteArrayList<Flow>()