package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange

@OptIn(ExperimentalTvMaterial3Api::class)
/**
 * Écran des paramètres de l'application. Permet de voir le serveur connecté et de se déconnecter.
 *
 * @param onLogout Callback pour la déconnexion.
 */
@Composable
fun SettingsScreen(onLogout: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val serverUrl by viewModel.currentServerUrl.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
                modifier = Modifier.width(600.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    "Paramètres",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Carte Serveur
            SettingsCard(title = "Serveur Actuel", icon = Icons.Default.Dns) {
                Text(
                        serverUrl ?: "Non connecté",
                        style = MaterialTheme.typography.headlineSmall,
                        color = PlexOrange,
                        fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Carte App
            SettingsCard(title = "Application", icon = Icons.Default.Info) {
                Text("Version 1.0.0 (Alpha)", color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(48.dp))

            // BOUTON DE DÉCONNEXION
            Button(
                    onClick = {
                        viewModel.disconnect()
                        onLogout()
                    },
                    colors =
                            ButtonDefaults.colors(
                                    containerColor = Color(0xFFD32F2F), // Rouge
                                    contentColor = Color.White,
                                    focusedContainerColor = Color(0xFFFF5252),
                                    focusedContentColor = Color.White
                            ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Changer de serveur")
            }
        }
    }
}

/** Composant carte générique pour les paramètres. */
@Composable
fun SettingsCard(
        title: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        content: @Composable () -> Unit
) {
    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(Color(0xFF1F1F1F), RoundedCornerShape(12.dp))
                            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
