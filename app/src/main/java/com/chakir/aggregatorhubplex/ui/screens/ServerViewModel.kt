package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.ServerHealth
import com.chakir.aggregatorhubplex.data.dto.ClientInfo
import com.chakir.aggregatorhubplex.data.dto.ServerInfo
import com.chakir.aggregatorhubplex.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ViewModel pour gérer l'affichage de l'état du serveur. Permet de vérifier périodiquement la santé
 * du serveur et de gérer les clients connectés.
 */
class ServerViewModel @Inject constructor(private val repository: ServerRepository) : ViewModel() {

    private val _serverHealth = MutableStateFlow<ServerHealth?>(null)
    val serverHealth: StateFlow<ServerHealth?> = _serverHealth
    
    private val _connectedServers = MutableStateFlow<List<ServerInfo>>(emptyList())
    val connectedServers: StateFlow<List<ServerInfo>> = _connectedServers
    
    private val _connectedClients = MutableStateFlow<List<ClientInfo>>(emptyList())
    val connectedClients: StateFlow<List<ClientInfo>> = _connectedClients
    
    private val _refreshStatus = MutableStateFlow<String?>(null)
    val refreshStatus: StateFlow<String?> = _refreshStatus

    init {
        checkHealth()
        loadDashboardData()
    }

    /** Lance une vérifications de l'état de santé du serveur et charge les données. */
    fun checkHealth() {
        viewModelScope.launch {
            repository.getServerHealth().collect { health -> _serverHealth.value = health }
        }
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _connectedServers.value = repository.getConnectedServers()
            _connectedClients.value = repository.getConnectedClients()
        }
    }
    
    fun triggerBackendRefresh() {
        viewModelScope.launch {
            _refreshStatus.value = "Démarrage du scan..."
            val success = repository.triggerRefresh()
            if (success) {
                _refreshStatus.value = "Scan démarré avec succès"
                // Recharger les données après un court délai
                loadDashboardData()
            } else {
                 _refreshStatus.value = "Erreur lors du scan"
            }
        }
    }
}
