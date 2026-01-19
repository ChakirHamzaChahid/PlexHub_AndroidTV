package com.chakir.aggregatorhubplex.util

/**
 * A generic sealed class that represents the state of a data request.
 * It can be in one of three states: Success, Error, or Loading.
 *
 * @param T The type of the data held by the resource.
 * @param data The actual data. Can be present in any state, for example, to show stale data while loading new data.
 * @param message An optional error message, typically used in the Error state.
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {

    /**
     * Represents a successful data request.
     * @param data The successfully retrieved data. It cannot be null.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents a failed data request.
     * @param message The error message describing what went wrong.
     * @param data Optional data that might still be relevant (e.g., cached data).
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Represents a data request that is currently in progress.
     * @param data Optional stale data that can be displayed while new data is being loaded.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
