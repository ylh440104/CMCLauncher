package com.cmc.launcher.coroutine

import kotlinx.coroutines.CompletableDeferred

class DataBridge<T> {
    private val deferred = CompletableDeferred<T>()

    suspend fun awaitData(): T {
        return deferred.await()
    }

    fun provideData(value: T) {
        if (!deferred.isCompleted) {
            deferred.complete(value)
        }
    }
}