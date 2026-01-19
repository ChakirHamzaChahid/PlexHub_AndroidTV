package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ViewModel pour gérer la découverte et la configuration du serveur. Vérifie l'URL du serveur et
 * gère l'état de connexion.
 */
class ServerDiscoveryViewModel @Inject constructor(private val prefsManager: PreferencesManager) :
        ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryState>(DiscoveryState.Loading)
    val uiState: StateFlow<DiscoveryState> = _uiState

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput

    init {
        checkServerUrl()
    }

    /** Vérifie si une URL de serveur est déjà enregistrée. */
    private fun checkServerUrl() {
        viewModelScope.launch {
            val url = prefsManager.serverUrl.first()
            if (url.isNullOrBlank()) {
                _uiState.value = DiscoveryState.InputNeeded
            } else {
                _uiState.value = DiscoveryState.Success
            }
        }
    }

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
    }

    /** Sauvegarde l'URL et tente la connexion. */
    fun saveAndConnect(url: String) {
        viewModelScope.launch {
            if (url.isNotBlank()) {
                prefsManager.setServerUrl(url)
                _uiState.value = DiscoveryState.Success
            } else {
                _uiState.value = DiscoveryState.Error("L'URL ne peut pas être vide")
            }
        }
    }
}

/** États possibles pour la découverte du serveur. */
sealed class DiscoveryState {
    object Loading : DiscoveryState()
    object InputNeeded : DiscoveryState()
    object Success : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}
