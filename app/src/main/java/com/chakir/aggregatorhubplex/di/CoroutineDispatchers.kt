package com.chakir.aggregatorhubplex.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface fournissant les dispatchers de coroutines pour l'application. Cette abstraction
 * facilite le remplacement des dispatchers lors des tests unitaires (par exemple, utiliser
 * `TestDispatcher` au lieu de `Dispatchers.IO`).
 */
interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
}

/** Implémentation de production de [CoroutineDispatchers]. */
class AppCoroutineDispatchers : CoroutineDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
}
