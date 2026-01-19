package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ViewModel pour l'écran des paramètres. Gère l'affichage des informations utilisateur et la
 * déconnexion.
 */
class SettingsViewModel @Inject constructor(private val prefsManager: PreferencesManager) :
        ViewModel() {

    // On affiche l'URL actuelle pour info
    val currentServerUrl: StateFlow<String?> =
            prefsManager.serverUrl.stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
            )

    /** Déconnecte l'utilisateur en effaçant les préférences. */
    fun disconnect() {
        viewModelScope.launch {
            prefsManager.clearAll() // Efface l'IP sauvegardée
        }
    }
}
