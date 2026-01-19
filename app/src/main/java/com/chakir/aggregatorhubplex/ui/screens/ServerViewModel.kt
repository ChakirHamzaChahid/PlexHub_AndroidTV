package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.ServerHealth
import com.chakir.aggregatorhubplex.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ViewModel pour gérer l'affichage de l'état du serveur. Permet de vérifier périodiquement la santé
 * du serveur.
 */
class ServerViewModel @Inject constructor(private val repository: ServerRepository) : ViewModel() {

    private val _serverHealth = MutableStateFlow<ServerHealth?>(null)
    val serverHealth: StateFlow<ServerHealth?> = _serverHealth

    init {
        checkHealth()
    }

    /** Lance une vérifications de l'état de santé du serveur. */
    fun checkHealth() {
        viewModelScope.launch {
            repository.getServerHealth().collect { health -> _serverHealth.value = health }
        }
    }
}
