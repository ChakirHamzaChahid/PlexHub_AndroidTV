package com.chakir.aggregatorhubplex.ui.screens

// Import Mobile spécifique pour le Loader (absent de TV M3)
// Import TV M3 global (Button, Text, MaterialTheme, etc.)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import com.chakir.aggregatorhubplex.data.ServerHealth
import com.chakir.aggregatorhubplex.data.ServerStatus

@OptIn(ExperimentalTvMaterial3Api::class)
/** Écran affichant l'état des serveurs et leur santé. Permet de rafraîchir manuellement l'état. */
@Composable
fun ServersScreen(viewModel: ServerViewModel = hiltViewModel()) {
    val health by viewModel.serverHealth.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141414)).padding(48.dp)) {
        Column {
            Text(
                    text = "État du Serveur",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            health?.let { server -> ServerHealthCard(server) }
                    ?: run { CircularProgressIndicator(color = Color(0xFFE5A00D)) }

            Spacer(modifier = Modifier.height(32.dp))

            // Bouton TV corrigé
            Button(
                    onClick = { viewModel.checkHealth() },
                    colors =
                            ButtonDefaults.colors(
                                    containerColor = Color(0xFF1F1F1F),
                                    contentColor = Color.White,
                                    focusedContainerColor = Color(0xFFE5A00D), // Important pour TV
                                    focusedContentColor = Color.Black
                            )
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualiser")
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
/**
 * Carte affichant les détails de santé d'un serveur.
 *
 * @param server Les informations de santé du serveur.
 */
@Composable
fun ServerHealthCard(server: ServerHealth) {
    val statusColor =
            when (server.status) {
                ServerStatus.ONLINE -> Color(0xFF4CAF50) // Vert
                ServerStatus.SLOW -> Color(0xFFFF9800) // Orange
                ServerStatus.OFFLINE -> Color(0xFFF44336) // Rouge
                ServerStatus.CHECKING -> Color.Gray
            }

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(Color(0xFF1F1F1F), RoundedCornerShape(12.dp))
                            .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                    server.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(server.url, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (server.latencyMs > 0) {
                Text(
                        "${server.latencyMs} ms",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Surface(
                    colors =
                            ClickableSurfaceDefaults.colors(
                                    containerColor = statusColor.copy(alpha = 0.2f),
                                    contentColor = statusColor
                            ),
                    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
                    onClick = {} // Surface non cliquable visuellement
            ) {
                Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector =
                                    if (server.status == ServerStatus.OFFLINE) Icons.Default.Warning
                                    else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                            server.status.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                    )
                }
            }
        }
    }
}
