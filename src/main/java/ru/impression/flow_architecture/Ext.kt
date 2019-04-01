package ru.impression.flow_architecture

@PublishedApi
internal val Class<*>.notNullName get() = canonicalName ?: throw IllegalArgumentException("Dont use anonymous classes")