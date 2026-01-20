package com.chakir.aggregatorhubplex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.chakir.aggregatorhubplex.domain.model.Movie
import com.chakir.aggregatorhubplex.ui.components.MovieCard
import com.chakir.aggregatorhubplex.ui.theme.PlexOrange
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
/**
 * Écran de recherche global. Permet de rechercher des films ou séries dans le catalogue.
 *
 * @param onMovieClick Callback lorsqu'un média est cliqué.
 * @param viewModel ViewModel partagé pour la recherche.
 */
@Composable
fun SearchScreen(onMovieClick: (Movie) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
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
        modifier =
            Modifier
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
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PlexOrange) },
            singleLine = true,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PlexOrange,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PlexOrange,
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
                    color = PlexOrange,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // On réutilise la grille standard mais sans header
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(6),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    items(searchResults.itemCount) { index ->
                        val movie = searchResults[index]
                        if (movie != null) {
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                                plexColor = PlexOrange,
                                modifier = Modifier.fillMaxWidth()
                            )
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
