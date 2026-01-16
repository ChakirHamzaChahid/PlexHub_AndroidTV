package com.chakir.aggregatorhubplex.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chakir.aggregatorhubplex.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: PreferencesManager
) : ViewModel() {

    // On affiche l'URL actuelle pour info
    val currentServerUrl: StateFlow<String?> = prefsManager.serverUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun disconnect() {
        viewModelScope.launch {
            prefsManager.clearAll() // Efface l'IP sauvegardée
        }
    }
}