package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.NetworkModule
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerDiscoveryViewModel @Inject constructor(
    private val prefsManager: PreferencesManager
) : ViewModel() {

    // États de l'écran
    private val _uiState = MutableStateFlow<DiscoveryState>(DiscoveryState.Loading)
    val uiState: StateFlow<DiscoveryState> = _uiState

    // Champ de texte pour l'URL
    private val _urlInput = MutableStateFlow("http://192.168.0.175:8186") // Valeur par défaut pour aider
    val urlInput: StateFlow<String> = _urlInput

    init {
        checkSavedServer()
    }

    // 1. Vérification au démarrage
    private fun checkSavedServer() {
        viewModelScope.launch {
            val savedUrl = prefsManager.serverUrl.first()
            if (!savedUrl.isNullOrEmpty()) {
                // URL trouvée -> On configure et on connecte direct
                NetworkModule.updateBaseUrl(savedUrl)
                _uiState.value = DiscoveryState.Success
            } else {
                // Pas d'URL -> On affiche le formulaire
                _uiState.value = DiscoveryState.InputNeeded
            }
        }
    }

    // 2. Action utilisateur : Sauvegarder
    fun saveAndConnect(url: String) {
        viewModelScope.launch {
            _uiState.value = DiscoveryState.Loading

            // Petit nettoyage de l'URL
            var cleanUrl = url.trim()
            if (!cleanUrl.startsWith("http")) {
                cleanUrl = "http://$cleanUrl"
            }
            if (!cleanUrl.endsWith(":8186") && !cleanUrl.endsWith("/")) {
                // On assume le port par défaut si pas précisé (optionnel)
                // cleanUrl += ":8186"
            }

            // Mise à jour du module Réseau
            try {
                NetworkModule.updateBaseUrl(cleanUrl)
                // Sauvegarde persistante
                prefsManager.setServerUrl(cleanUrl)
                _uiState.value = DiscoveryState.Success
            } catch (e: Exception) {
                _uiState.value = DiscoveryState.Error("URL invalide : ${e.message}")
            }
        }
    }

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
    }
}

// États simples pour l'UI
sealed class DiscoveryState {
    object Loading : DiscoveryState()
    object InputNeeded : DiscoveryState()
    object Success : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}