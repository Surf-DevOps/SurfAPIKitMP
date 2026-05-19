package io.github.surfdevops.surfapikit.core

sealed class LoadState<out T> {
    data object Idle : LoadState<Nothing>()
    data object Loading : LoadState<Nothing>()
    data class Success<T>(val value: T) : LoadState<T>()
    data class Failure(val error: ApiError) : LoadState<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val valueOrNull: T? get() = (this as? Success)?.value
    val errorOrNull: ApiError? get() = (this as? Failure)?.error
}

object EmptyResponse
