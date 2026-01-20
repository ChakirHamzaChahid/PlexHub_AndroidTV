package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.chakir.aggregatorhubplex.data.ServerHealth
import com.chakir.aggregatorhubplex.data.ServerStatus
import com.chakir.aggregatorhubplex.data.dto.ClientInfo
import com.chakir.aggregatorhubplex.data.dto.ServerInfo
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange

@OptIn(ExperimentalTvMaterial3Api::class)
/** Écran tableau de bord affichant l'état des serveurs, les clients et permettant le refresh. */
@Composable
fun ServersScreen(viewModel: ServerViewModel = hiltViewModel()) {
    val health by viewModel.serverHealth.collectAsState()
    val servers by viewModel.connectedServers.collectAsState()
    val clients by viewModel.connectedClients.collectAsState()
    val refreshStatus by viewModel.refreshStatus.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141414)).padding(48.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tableau de Bord",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Bouton Refresh
                Button(
                    onClick = { viewModel.triggerBackendRefresh() },
                    colors =
                        ButtonDefaults.colors(
                            containerColor = Color(0xFF1F1F1F),
                            contentColor = Color.White,
                            focusedContainerColor = PlexOrange,
                            focusedContentColor = Color.Black
                        )
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Forcer Scan")
                }
            }
            
            if (refreshStatus != null) {
                Text(
                    text = refreshStatus!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = PlexOrange,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                // Section Santé (Principal)
                item {
                    SectionTitle("État du Backend")
                    Spacer(modifier = Modifier.height(16.dp))
                    health?.let { server -> ServerHealthCard(server) }
                        ?: CircularProgressIndicator(color = PlexOrange)
                }

                // Section Serveurs Plex Connectés
                item {
                    SectionTitle("Serveurs Plex Connectés")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (servers.isEmpty()) {
                     item { Text("Aucun serveur détecté", color = Color.Gray) }
                } else {
                    items(servers) { server ->
                         ServerInfoCard(server)
                         Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Section Clients Connectés
                item {
                    SectionTitle("Clients (Lecteurs)")
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (clients.isEmpty()) {
                    item { Text("Aucun client actif", color = Color.Gray) }
                } else {
                    items(clients) { client ->
                         ClientInfoCard(client)
                         Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.LightGray,
        fontWeight = FontWeight.SemiBold
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ServerHealthCard(server: ServerHealth) {
    val statusColor =
        when (server.status) {
            ServerStatus.ONLINE -> Color(0xFF4CAF50) // Vert
            ServerStatus.SLOW -> Color(0xFFFF9800) // Orange
            ServerStatus.OFFLINE -> Color(0xFFF44336) // Rouge
            ServerStatus.CHECKING -> Color.Gray
        }

    Card(
        onClick = {}, // Required for TV Card
        colors = CardDefaults.colors(containerColor = Color(0xFF1F1F1F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
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
                StatusBadge(server.status.name, statusColor)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ServerInfoCard(server: ServerInfo) {
    Card(
        onClick = {}, // Required for TV Card
        colors = CardDefaults.colors(containerColor = Color(0xFF252525)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Dns, null, tint = PlexOrange)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                 Text(server.name, color = Color.White, fontWeight = FontWeight.Bold)
                 Text(server.url, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            StatusBadge(server.status, if (server.status == "Online") Color(0xFF4CAF50) else Color.Gray)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ClientInfoCard(client: ClientInfo) {
    Card(
         onClick = {}, // Required for TV Card
         colors = CardDefaults.colors(containerColor = Color(0xFF252525)),
         modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if(client.product == "Plex Media Server") Icons.Default.Dns else Icons.Default.Devices, null, tint = Color.LightGray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(client.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${client.product} • ${client.device}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            StatusBadge(client.status, if (client.status == "playing") PlexOrange else Color.Gray)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        colors = ClickableSurfaceDefaults.colors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        ),
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
