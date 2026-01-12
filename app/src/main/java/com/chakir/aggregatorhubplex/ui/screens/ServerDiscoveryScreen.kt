package com.chakir.aggregatorhubplex.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.chakir.aggregatorhubplex.data.NetworkDiscovery
import com.chakir.aggregatorhubplex.data.NetworkModule

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ServerDiscoveryScreen(onServerFound: () -> Unit) {
    val context = LocalContext.current
    val discovery = remember { NetworkDiscovery(context) }
    val prefs = remember { context.getSharedPreferences("plexhub_prefs", Context.MODE_PRIVATE) }

    // On récupère l'URL sauvegardée au chargement
    val savedUrl = remember { prefs.getString("base_url", null) }

    // États
    // Si on a une URL, on ne montre pas la saisie par défaut (mode "Resume")
    // Sinon, on montre la saisie directe (mode "Setup")
    var showManualInput by remember { mutableStateOf(savedUrl == null) }

    // Champ de texte pré-rempli avec l'URL sauvegardée si elle existe, sinon vide
    var manualIpInput by remember { mutableStateOf(savedUrl ?: "") }

    // Fonction pour valider et passer à la suite
    fun connectToServer(url: String) {
        val formattedUrl = discovery.formatManualAddress(url)
        if (formattedUrl != null) {
            // 1. Mettre à jour Retrofit
            NetworkModule.updateBaseUrl(formattedUrl)
            // 2. Sauvegarder pour le prochain démarrage
            prefs.edit().putString("base_url", formattedUrl).apply()
            // 3. Go Home
            onServerFound()
        } else {
            Toast.makeText(context, "Adresse invalide", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).width(500.dp) // Largeur fixe pour TV
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFE5A00D)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!showManualInput && savedUrl != null) {
                // --- CAS 1 : URL DÉJÀ SAUVEGARDÉE ---
                Text(
                    text = "Serveur enregistré",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = savedUrl,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { connectToServer(savedUrl) },
                    colors = ButtonDefaults.colors(containerColor = Color(0xFFE5A00D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuer", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showManualInput = true },
                    colors = ButtonDefaults.colors(containerColor = Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modifier l'adresse")
                }

            } else {
                // --- CAS 2 : PREMIER DÉMARRAGE OU MODIFICATION ---
                Text(
                    text = "Configuration du Serveur",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Entrez l'adresse IP de votre serveur PlexHub (ex: 192.168.1.50)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = manualIpInput,
                    onValueChange = { manualIpInput = it },
                    label = { Text("Adresse IP", color = Color.Gray) },
                    placeholder = { Text("192.168.x.x", color = Color.DarkGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE5A00D),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFE5A00D)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { connectToServer(manualIpInput) },
                    colors = ButtonDefaults.colors(containerColor = Color(0xFFE5A00D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connexion", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                // Bouton Annuler (Seulement si on avait déjà une URL valide avant)
                if (savedUrl != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showManualInput = false }, // Retour à l'écran "Continuer"
                        colors = ButtonDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler")
                    }
                }
            }
        }
    }
}