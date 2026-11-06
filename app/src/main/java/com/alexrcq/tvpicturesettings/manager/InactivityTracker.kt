package com.alexrcq.tvpicturesettings.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest

class InactivityTracker(
    private val idleDurationMs: Flow<Long>,
    timeoutMinutes: Flow<Long>,
    private val condition: () -> Boolean = { true }
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<State> = timeoutMinutes
        .transformLatest { timeout ->
            if (timeout <= 0L) {
                emit(State.NONE)
                return@transformLatest
            }

            val timeoutMs = timeout * 60_000L
            val warningStartMs = timeoutMs - WARNING_DURATION_MS
            var baselineIdleMs = 0L

            idleDurationMs.collect { idleMs ->
                val currentState = when {
                    idleMs < warningStartMs -> {
                        baselineIdleMs = 0L
                        State.NONE
                    }
                    !condition() -> {
                        baselineIdleMs = idleMs
                        State.NONE
                    }
                    idleMs - baselineIdleMs < warningStartMs -> State.NONE
                    idleMs - baselineIdleMs >= timeoutMs -> State.TIMEOUT
                    else -> State.WARNING
                }
                emit(currentState)
            }
        }
        .onStart { emit(State.NONE) }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    enum class State {
        NONE, WARNING, TIMEOUT
    }

    companion object {
        private const val WARNING_DURATION_MS = 15_000L
    }
}
