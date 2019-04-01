package ru.impression.flow_architecture

val Class<*>.notNullName get() = canonicalName ?: throw IllegalArgumentException("Dont use anonymous classes")
