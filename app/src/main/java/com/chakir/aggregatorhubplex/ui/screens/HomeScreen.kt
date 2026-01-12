package com.chakir.aggregatorhubplex.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.tv.material3.*
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chakir.aggregatorhubplex.data.Movie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Couleurs
val PlexAccent = Color(0xFFE5A00D)
val NetflixBlack = Color(0xFF141414)
val DarkSurface = Color(0xFF1F1F1F)
val TextWhite = Color(0xFFE5E5E5)
val TextGrey = Color(0xFFB3B3B3)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // 1. OBSERVATION DES DONNÉES
    val movies = viewModel.moviesPagingFlow.collectAsLazyPagingItems()
    val totalCount by viewModel.totalCount.collectAsState()

    // États UI locaux
    var isSearchOpen by remember { mutableStateOf(false) }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    var tempSearchQuery by remember { mutableStateOf("") }

    // États Filtres UI
    var currentSortLabel by remember { mutableStateOf(SortOption.ADDED_DESC.label) }
    val currentType by viewModel.currentFilterType.collectAsState()
    val currentSortOption by viewModel.currentSortOption.collectAsState()

    // --- MODIFICATION 1 : Gestionnaire de Scroll Intelligent ---
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Ce compteur sert à forcer le scroll en haut UNIQUEMENT quand on change un filtre
    var filterVersion by remember { mutableIntStateOf(0) }

    val searchButtonFocusRequester = remember { FocusRequester() }
    val searchKeyboardFocusRequester = remember { FocusRequester() }
    val sortMenuFocusRequester = remember { FocusRequester() }

    // Scroll en haut uniquement si la version du filtre change (pas au retour de détail)
    LaunchedEffect(filterVersion) {
        if (filterVersion > 0) {
            gridState.scrollToItem(0)
        }
    }

    // Gestion focus et retour
    LaunchedEffect(isSearchOpen) { if(isSearchOpen) { delay(100); searchKeyboardFocusRequester.requestFocus() } }
    LaunchedEffect(isSortMenuOpen) { if(isSortMenuOpen) { delay(100); sortMenuFocusRequester.requestFocus() } }

    // --- MODIFICATION 2 : Navigation "Back" améliorée (Ergonomie TV) ---
    // Détecte si la liste est scrollée vers le bas
    val isListScrolled by remember { derivedStateOf { gridState.firstVisibleItemIndex > 0 } }

    BackHandler(enabled = isListScrolled || isSearchOpen || isSortMenuOpen) {
        when {
            isSearchOpen -> isSearchOpen = false
            isSortMenuOpen -> isSortMenuOpen = false
            isListScrolled -> {
                // Si on appuie sur retour et qu'on est en bas, on remonte tout en haut
                scope.launch {
                    gridState.animateScrollToItem(0)
                    // On redonne le focus à la recherche pour faciliter la navigation
                    searchButtonFocusRequester.requestFocus()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(NetflixBlack)) {

        Column(modifier = Modifier.fillMaxSize().padding(start = 48.dp, top = 32.dp, end = 48.dp, bottom = 0.dp)) {

            // --- 1. HEADER FIXE ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Compteur
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Plex", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.background(PlexAccent, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp)) {
                            Text("HUB", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black), color = Color.Black)
                        }
                    }
                    Text(
                        "$totalCount TITRES",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGrey,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Boutons d'action
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // FILTRE TYPE
                    Row(modifier = Modifier.background(DarkSurface, RoundedCornerShape(50)).border(1.dp, Color(0xFF333333), RoundedCornerShape(50)).padding(4.dp)) {

                        // --- MODIFICATION 3 : Incrémentation de filterVersion sur les clics ---
                        TypeButton(Icons.Default.PlayArrow, "Films", currentType == "movie") {
                            viewModel.onTypeChange("movie")
                            filterVersion++
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TypeButton(Icons.Default.List, "Séries", currentType == "show") {
                            viewModel.onTypeChange("show")
                            filterVersion++ // Reset scroll
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TypeButton(Icons.Default.Home, "Tout", currentType == null) {
                            viewModel.onTypeChange("all")
                            filterVersion++
                        }
                    }

                    // RECHERCHE
                    IconButton(
                        onClick = { isSearchOpen = true },
                        modifier = Modifier.focusRequester(searchButtonFocusRequester),
                        colors = IconButtonDefaults.colors(containerColor = Color.Transparent, contentColor = TextGrey, focusedContainerColor = DarkSurface, focusedContentColor = TextWhite)
                    ) {
                        Icon(Icons.Default.Search, "Rechercher", modifier = Modifier.size(28.dp))
                    }

                    // TRI
                    Button(
                        onClick = { isSortMenuOpen = true },
                        colors = ButtonDefaults.colors(containerColor = Color.Transparent, contentColor = TextGrey, focusedContainerColor = DarkSurface, focusedContentColor = TextWhite),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentSortLabel.substringBefore(" "), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            }

            // --- 2. GRILLE DÉROULANTE ---

            if (movies.itemCount == 0 && movies.loadState.refresh !is LoadState.Loading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Aucun résultat dans la bibliothèque locale.", color = TextGrey)
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 48.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        count = movies.itemCount,
                        key = movies.itemKey { it.id },
                        contentType = movies.itemContentType { "movie" }
                    ) { index ->
                        val movie = movies[index]
                        if (movie != null) {
                            MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                        }
                    }

                    if (movies.loadState.append is LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(30.dp), color = PlexAccent)
                            }
                        }
                    }
                }
            }
        }

        // --- OVERLAYS ---
        AnimatedVisibility(visible = isSearchOpen, enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).clickable { isSearchOpen = false }, contentAlignment = Alignment.CenterEnd) {
                Column(modifier = Modifier.fillMaxHeight().width(400.dp).background(DarkSurface).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rechercher", style = MaterialTheme.typography.headlineSmall, color = PlexAccent)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = tempSearchQuery.ifEmpty { "Tapez votre recherche..." },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (tempSearchQuery.isEmpty()) Color.Gray else Color.White,
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    VirtualKeyboard(
                        onKeyPress = {
                            tempSearchQuery += it
                            viewModel.onSearchChange(tempSearchQuery)
                            filterVersion++ // Scroll en haut pour voir les résultats
                        },
                        onBackspace = {
                            if (tempSearchQuery.isNotEmpty()) {
                                tempSearchQuery = tempSearchQuery.dropLast(1)
                                viewModel.onSearchChange(tempSearchQuery)
                                filterVersion++
                            }
                        },
                        onSpace = {
                            tempSearchQuery += " "
                            viewModel.onSearchChange(tempSearchQuery)
                        },
                        onClose = { isSearchOpen = false },
                        focusRequester = searchKeyboardFocusRequester
                    )
                }
            }
        }

        AnimatedVisibility(visible = isSortMenuOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable { isSortMenuOpen = false }, contentAlignment = Alignment.CenterEnd) {
                Column(modifier = Modifier.fillMaxHeight().width(350.dp).background(DarkSurface).padding(32.dp), horizontalAlignment = Alignment.Start) {
                    Text("Trier par", style = MaterialTheme.typography.headlineSmall, color = PlexAccent, modifier = Modifier.padding(bottom = 16.dp))
                    SortOption.values().forEach { option ->
                        SortMenuItem(
                            label = option.label,
                            isSelected = currentSortLabel == option.label,
                            onClick = {
                                viewModel.onSortChange(option)
                                currentSortLabel = option.label
                                isSortMenuOpen = false
                                filterVersion++ // Reset scroll
                            },
                            modifier = if (currentSortLabel == option.label) Modifier.focusRequester(sortMenuFocusRequester) else Modifier
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// --- COMPOSANTS UI HELPERS (Inchangés) ---

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.08f else 1f, label = "scale")
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(110.dp).scale(scale)) {
        Box(
            modifier = Modifier
                .height(165.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .focusable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (isFocused) {
                Box(modifier = Modifier.fillMaxSize().border(3.dp, TextWhite, RoundedCornerShape(6.dp)))
            }

            if (movie.hasMultipleSources) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(PlexAccent, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text("MULTI", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            // --- MODIFICATION ICI : Affichage de la note IMDb ---
            // On priorise imdbRating, sinon on utilise rating (Plex), sinon 0
            val displayRating = movie.imdbRating ?: movie.rating

            if (displayRating != null && displayRating > 0) {
                Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                    // On affiche juste la note avec l'étoile
                    Text("★ $displayRating", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PlexAccent)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            color = if (isFocused) TextWhite else TextGrey,
            maxLines = 1,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TypeButton(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (isSelected) PlexAccent else Color.Transparent, contentColor = if (isSelected) Color.Black else TextGrey, focusedContainerColor = if (isSelected) PlexAccent else Color(0xFF333333), focusedContentColor = if (isSelected) Color.Black else TextWhite),
        modifier = Modifier.height(32.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VirtualKeyboard(onKeyPress: (String) -> Unit, onBackspace: () -> Unit, onSpace: () -> Unit, onClose: () -> Unit, focusRequester: FocusRequester) {
    val rows = listOf(listOf("A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"), listOf("Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"), listOf("W", "X", "C", "V", "B", "N", "1", "2", "3", "4"))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEachIndexed { r, row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEachIndexed { c, k -> KeyButton(text = k, onClick = { onKeyPress(k) }, modifier = (if (r == 0 && c == 0) Modifier.focusRequester(focusRequester) else Modifier).weight(1f)) } } }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSpace, colors = ButtonDefaults.colors(containerColor = DarkSurface, focusedContainerColor = TextWhite), shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)), modifier = Modifier.weight(2f)) { Text("ESPACE", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Button(onClick = onBackspace, colors = ButtonDefaults.colors(containerColor = DarkSurface, focusedContainerColor = Color(0xFFD32F2F)), shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)), modifier = Modifier.weight(1f)) { Icon(Icons.Default.ArrowBack, contentDescription = "Effacer") }
            Button(onClick = onClose, colors = ButtonDefaults.colors(containerColor = PlexAccent, contentColor = Color.Black), shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)), modifier = Modifier.weight(1f)) { Icon(Icons.Default.Check, contentDescription = "Valider") }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KeyButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, colors = ButtonDefaults.colors(containerColor = Color(0xFF2B2B2B), contentColor = TextWhite, focusedContainerColor = TextWhite, focusedContentColor = Color.Black), shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)), contentPadding = PaddingValues(0.dp), modifier = modifier.aspectRatio(1f)) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SortMenuItem(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, colors = ClickableSurfaceDefaults.colors(containerColor = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent, contentColor = if (isSelected) PlexAccent else TextGrey, focusedContainerColor = PlexAccent, focusedContentColor = Color.Black), shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(8.dp)), modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            if (isSelected) { Icon(Icons.Default.Check, contentDescription = null, tint = PlexAccent) }
        }
    }
}