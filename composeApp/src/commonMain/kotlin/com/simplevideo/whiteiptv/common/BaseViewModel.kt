package com.simplevideo.whiteiptv.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

abstract class BaseViewModel<State : Any, Action, Event>(initialState: State) : ViewModel() {
    private val _viewStates = MutableStateFlow(initialState)
    private val _viewActions = MutableSharedFlow<Action?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun viewStates(): StateFlow<State> = _viewStates.asStateFlow()
    fun viewActions(): SharedFlow<Action?> = _viewActions.asSharedFlow()

    protected var viewState: State
        get() = _viewStates.value
        set(value) {
            _viewStates.value = value
        }

    protected var viewAction: Action?
        get() = _viewActions.replayCache.lastOrNull()
        set(value) {
            _viewActions.tryEmit(value)
        }

    open fun obtainEvent(viewEvent: Event) {}

    fun clearAction() {
        viewAction = null
    }
}
