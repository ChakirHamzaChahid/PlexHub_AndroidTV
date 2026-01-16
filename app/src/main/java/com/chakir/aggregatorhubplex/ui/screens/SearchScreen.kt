package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.chakir.aggregatorhubplex.data.Movie
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // On observe les résultats (filtrés par la query du ViewModel)
    val searchResults = viewModel.moviesPagingFlow.collectAsLazyPagingItems()
    val searchQuery by viewModel.currentSearchQuery.collectAsState()

    // Gestion du focus pour le champ de texte
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Au lancement, on focus le champ de recherche et on s'assure que le filtre est "Tout"
    LaunchedEffect(Unit) {
        viewModel.onTypeChange("all") // Recherche globale
        delay(100)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(start = 24.dp, top = 24.dp, end = 48.dp, bottom = 24.dp)
    ) {
        // --- ZONE DE SAISIE ---
        Text(
            "Recherche",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = searchQuery ?: "",
            onValueChange = { viewModel.onSearchChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("Titre, acteur, réalisateur...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PlexAccent) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlexAccent,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = PlexAccent,
                focusedContainerColor = Color(0xFF1F1F1F),
                unfocusedContainerColor = Color(0xFF1F1F1F)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- RÉSULTATS ---
        if (searchQuery.isNullOrBlank()) {
            // État vide
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Tapez quelque chose pour chercher dans votre bibliothèque",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Grille de résultats (Réutilisation de votre grille existante ou version simplifiée)
            // On affiche le nombre de résultats trouvés (approximation via paging)
            if (searchResults.itemCount > 0) {
                Text(
                    "Résultats",
                    style = MaterialTheme.typography.titleMedium,
                    color = PlexAccent,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // On réutilise la grille standard mais sans header
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    items(searchResults.itemCount) { index ->
                        val movie = searchResults[index]
                        if (movie != null) {
                            MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucun résultat trouvé pour \"$searchQuery\"", color = Color.Gray)
                }
            }
        }
    }
}