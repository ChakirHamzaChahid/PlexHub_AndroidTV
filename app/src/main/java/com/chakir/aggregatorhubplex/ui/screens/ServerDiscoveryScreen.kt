package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api

// Couleurs (Vos couleurs personnalisées)
val BackgroundColor = Color(0xFF141414)
val BrandColor = Color(0xFFE5A00D)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ServerDiscoveryScreen(
    onServerFound: () -> Unit,
    viewModel: ServerDiscoveryViewModel = hiltViewModel() // Injection Hilt
) {
    // On observe l'état du ViewModel (qui gère DataStore et NetworkModule)
    val uiState by viewModel.uiState.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()

    // 1. Réaction aux états : Si succès -> Navigation vers l'accueil
    LaunchedEffect(uiState) {
        if (uiState is DiscoveryState.Success) {
            onServerFound()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            // Cas 1 : Vérification initiale ou Connexion en cours
            is DiscoveryState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BrandColor)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Connexion au serveur...",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Cas 2 : Succès (Transition invisible)
            is DiscoveryState.Success -> {
                // Rien à afficher, le LaunchedEffect gère la navigation
            }

            // Cas 3 : Saisie nécessaire (Premier lancement) ou Erreur
            is DiscoveryState.InputNeeded, is DiscoveryState.Error -> {
                val errorMessage = (state as? DiscoveryState.Error)?.message

                // On réutilise votre mise en page propre
                DiscoveryContent(
                    urlValue = urlInput,
                    errorMessage = errorMessage,
                    onUrlChange = viewModel::onUrlChanged,
                    onConnect = { viewModel.saveAndConnect(urlInput) }
                )
            }
        }
    }
}

@Composable
fun DiscoveryContent(
    urlValue: String,
    errorMessage: String?,
    onUrlChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(32.dp)
            .width(500.dp) // Largeur fixe optimisée TV
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Configuration du Serveur",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Entrez l'adresse IP de votre serveur PlexHub\n(ex: 192.168.1.50)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Champ de saisie
        OutlinedTextField(
            value = urlValue,
            onValueChange = onUrlChange,
            label = { Text("Adresse IP", color = Color.Gray) },
            placeholder = { Text("192.168.x.x", color = Color.DarkGray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColor,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = BrandColor,
                focusedLabelColor = BrandColor,
                unfocusedLabelColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onConnect()
            })
        )

        // Affichage d'erreur si besoin
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton Connexion
        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                "Connexion",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}