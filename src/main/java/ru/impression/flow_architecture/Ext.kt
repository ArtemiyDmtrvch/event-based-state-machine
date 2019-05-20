package ru.impression.flow_architecture

@PublishedApi
internal val Class<*>.notNullName
    get() = canonicalName ?: throw IllegalArgumentException("Don't use anonymous classes")