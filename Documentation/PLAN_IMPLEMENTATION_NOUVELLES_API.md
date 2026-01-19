# 📋 PLAN D'IMPLÉMENTATION - NOUVELLES FONCTIONNALITÉS ANDROID TV
**Intégration des nouvelles API Backend Python (FastAPI)**

**Date:** 19 Janvier 2026  
**Basé sur:** API_ANDROID.md v1.0  
**Cible:** Android TV - Jetpack Compose  

---

## 📑 TABLE DES MATIÈRES

1. [Vue d'ensemble des nouvelles API](#vue-densemble)
2. [Analyse d'impact](#analyse-dimpact)
3. [Plan d'implémentation détaillé](#plan-détaillé)
4. [Sprints recommandés](#sprints)
5. [Dépendances et intégrations](#dépendances)
6. [Risques et mitigations](#risques)

---

## <a name="vue-densemble"></a>1️⃣ VUE D'ENSEMBLE DES NOUVELLES API

### 🎯 Nouvelles API identifiées dans le backend Python

#### **A. Endpoints de Découverte Enrichis**

| Endpoint | Statut | Priorité | Description |
|----------|--------|----------|-------------|
| `GET /api/recently-added` | ✨ NEW | **HAUTE** | Derniers médias ajoutés (Limit: 50) |
| `GET /api/hubs` | ✨ NEW | **HAUTE** | Hubs recommandés Plex (ex: "Recently Released", "Top Rated") |
| `GET /api/continue_watching` | ✨ NEW | **HAUTE** | Reprendre la lecture (On Deck) |
| `GET /api/watch-history` | ✨ NEW | **MOYENNE** | Historique de visionnage complet |
| `GET /api/search` (avancé) | ✨ ENHANCED | **HAUTE** | Recherche avancée avec `title`, `year`, `unwatched` |

#### **B. Endpoints d'Actions Utilisateur Enrichis**

| Endpoint | Méthode | Priorité | Description |
|----------|---------|----------|-------------|
| `/api/actions/scrobble` | POST | **HAUTE** | Marquer vu/non-vu |
| `/api/actions/progress` | POST | **HAUTE** | Mettre à jour progression (time_ms) |
| `/api/favorite/{media_id}` | POST | **MOYENNE** | Ajouter aux favoris |
| `/api/rate/{media_id}/{rating}` | POST | **MOYENNE** | Noter un média (0-10) |
| `/api/label/{media_id}/{label}` | POST/DELETE | **BASSE** | Gérer les labels personnalisés |
| `/api/optimize/{media_id}` | POST | **BASSE** | Lancer transcodage mobile |

#### **C. Endpoints Système & Monitoring**

| Endpoint | Priorité | Description |
|----------|----------|-------------|
| `GET /api/now-playing` | **MOYENNE** | Sessions actuelles (qui regarde quoi) |
| `GET /api/clients` | **BASSE** | Clients connectés |
| `GET /api/servers` | **BASSE** | Liste serveurs (déjà implémenté ?) |
| `GET /proxy-image` | **HAUTE** | Proxy images avec cache & redimensionnement |

#### **D. Nouveaux Data Models**

- `Source` : URL streaming, résolution, format (M3U, deep link Plex)
- `EpisodeDetail` : Episodes avec sources individuelles
- `SeasonDetail` : Saisons avec liste épisodes
- `Marker` : Intro/Credits (start_time, end_time en ms)
- `Chapter` : Chapitres (pour les longs contenus)
- `Trailer` : Trailers disponibles
- `Collection` : Collections Plex
- `ClientInfo`, `SessionInfo` : Info clients/sessions
- Supports enrichis : `AudioTrack`, `Subtitle`, badges techniques (4K, HDR, Atmos)

---

## <a name="analyse-dimpact"></a>2️⃣ ANALYSE D'IMPACT

### 🎬 Impact sur les Écrans Existants

| Écran | Changements | Nouvelles Données |
|-------|-------------|-------------------|
| **HomeScreen** | ✅ Nouveau hub "Hubs Recommandés" en haut | Appels `/api/hubs` |
| **DetailScreen** | ✅ Section "Reprendre la lecture" si en cours | Markers (intro/credits), Chapitres, Trailers |
| **PlayerScreen** | ✅ Affichage des chapiters, intro/credits skippable | Sauts automatiques, UI timeline améliorée |
| **FavoritesScreen** | ✅ Statut "favori" persiste | POST `/api/favorite` |
| **SearchScreen** | ✅ Filtres avancés (non-vus, année) | `/api/search` enrichi |
| **SettingsScreen** | ✅ Nouvelle section "Historique" | Paramètres sync |
| **NEW: HistoryScreen** | 🆕 Nouvel écran | Affichage `/api/watch-history` |
| **NEW: ContinueWatchingHub** | 🆕 Hub dédié | Carousel "Reprendre" sur HomeScreen |
| **NEW: PlaybackInfoScreen** | 🆕 Info lecteur temps réel | Affichage des badges, audio/sous-titres |

### 🔧 Impact Technique

```
Layer Impact:
┌─────────────────────────────────────────────────────────────┐
│ UI Composables (Screens + Components) - MOYENNES CHANGES    │
│ · NewHubsSection · ContinueWatchingCarousel · PlayerMarkers │
│ · HistoryScreen · RatingDialog · LabelManager               │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│ ViewModels - NOUVELLES VIEWMODELS + EXTENSIONS             │
│ · HomeViewModel (add hubs, continue) · DetailViewModel+ext │
│ · PlayerViewModel (markers, chapters, scrobble) · NEW       │
│ · HistoryViewModel · RatingViewModel · WatchNowViewModel   │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│ Repositories - NOUVELLES MÉTHODES                           │
│ · MediaRepository: getRecentlyAdded(), getHubs(),           │
│   getContinueWatching(), getWatchHistory()                  │
│ · PlaybackRepository: NEW (scrobble, progress, rating)      │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│ Data Sources - NOUVEAUX ENDPOINTS API                       │
│ · MovieApiService: 7+ nouveaux endpoints                    │
│ · Proxy image optimization                                  │
│ · Error handling & retry logic                              │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│ Database (Room) - NOUVELLES TABLES                          │
│ · WatchHistory, Markers, Chapters, Trailers, Ratings,       │
│ · Labels, ContinueWatchingCache                             │
└─────────────────────────────────────────────────────────────┘
```

---

## <a name="plan-détaillé"></a>3️⃣ PLAN D'IMPLÉMENTATION DÉTAILLÉ

### 🏗️ PHASE 1 : INFRASTRUCTURE (2-3 jours)

#### **Tâche 1.1 : Étendre MovieApiService** (Retrofit)
**Fichier:** `data/remote/MovieApiService.kt`

```kotlin
// NOUVEAUX ENDPOINTS
interface MovieApiService {
    // Existants
    @GET("api/movies")
    suspend fun getMovies(@Query("page") page: Int, ...): List<MediaDetail>
    
    // NOUVEAUX
    @GET("api/recently-added")
    suspend fun getRecentlyAdded(@Query("limit") limit: Int = 50): List<MediaDetail>
    
    @GET("api/hubs")
    suspend fun getHubs(@Query("limit") limit: Int = 10): Map<String, List<MediaDetail>>
    
    @GET("api/continue_watching")
    suspend fun getContinueWatching(): List<MediaDetail>
    
    @GET("api/search")
    suspend fun advancedSearch(
        @Query("title") title: String?,
        @Query("year") year: Int?,
        @Query("unwatched") unwatched: Boolean?,
        @Query("limit") limit: Int = 50
    ): List<MediaDetail>
    
    @GET("api/watch-history")
    suspend fun getWatchHistory(): List<HistoryEntry>
    
    @GET("api/now-playing")
    suspend fun getNowPlaying(): List<SessionInfo>
    
    // ACTIONS UTILISATEUR
    @POST("api/actions/scrobble")
    suspend fun markWatched(@Body request: ScrobbleRequest)
    
    @POST("api/actions/progress")
    suspend fun updateProgress(@Body request: ProgressRequest)
    
    @POST("api/favorite/{media_id}")
    suspend fun addFavorite(@Path("media_id") mediaId: String)
    
    @POST("api/rate/{media_id}/{rating}")
    suspend fun rateMedia(@Path("media_id") mediaId: String, @Path("rating") rating: Float)
    
    @POST("api/label/{media_id}/{label}")
    suspend fun addLabel(@Path("media_id") mediaId: String, @Path("label") label: String)
    
    @DELETE("api/label/{media_id}/{label}")
    suspend fun removeLabel(@Path("media_id") mediaId: String, @Path("label") label: String)
}
```

**Dépendances ajoutées:**
- Data models: `ScrobbleRequest`, `ProgressRequest` (DTOs)

---

#### **Tâche 1.2 : Créer nouvelles Tables Room**
**Fichier:** `data/local/db/`

```kotlin
// Nouvelles entités Room
@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // 'movie' ou 'show'
    val watchedAt: Long,
    val viewOffset: Int,
    val duration: Int,
    val thumbUrl: String?
)

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: String,
    val title: String,
    val type: String, // 'intro' ou 'credits'
    val startTimeMs: Int,
    val endTimeMs: Int
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: String,
    val title: String,
    val startTimeMs: Int,
    val endTimeMs: Int,
    val thumbUrl: String?
)

@Entity(tableName = "user_ratings")
data class UserRatingEntity(
    @PrimaryKey val mediaId: String,
    val rating: Float,
    val createdAt: Long
)

@Entity(tableName = "user_labels")
data class UserLabelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "media_id") val mediaId: String,
    @ColumnInfo(name = "label_name") val labelName: String
)

@Entity(tableName = "continue_watching_cache")
data class ContinueWatchingEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val type: String,
    val posterUrl: String,
    val viewOffset: Int,
    val duration: Int,
    val progressPercent: Float,
    val lastViewedAt: Long
)
```

**DAOs:**
```kotlin
@Dao
interface WatchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WatchHistoryEntity)
    
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT :limit")
    fun getHistory(limit: Int = 100): Flow<List<WatchHistoryEntity>>
    
    @Query("DELETE FROM watch_history WHERE watchedAt < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)
}

@Dao
interface MarkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(marker: MarkerEntity)
    
    @Query("SELECT * FROM markers WHERE mediaId = :mediaId")
    fun getMarkersForMedia(mediaId: String): Flow<List<MarkerEntity>>
}

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)
    
    @Query("SELECT * FROM chapters WHERE mediaId = :mediaId ORDER BY startTimeMs ASC")
    fun getChaptersForMedia(mediaId: String): Flow<List<ChapterEntity>>
}

@Dao
interface UserRatingDao {
    @Upsert
    suspend fun upsertRating(rating: UserRatingEntity)
    
    @Query("SELECT rating FROM user_ratings WHERE mediaId = :mediaId")
    suspend fun getRating(mediaId: String): Float?
}

@Dao
interface UserLabelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLabel(label: UserLabelEntity)
    
    @Query("SELECT labelName FROM user_labels WHERE mediaId = :mediaId")
    fun getLabels(mediaId: String): Flow<List<String>>
    
    @Delete
    suspend fun removeLabel(label: UserLabelEntity)
}
```

---

#### **Tâche 1.3 : Mettre à jour AppDatabase**
**Fichier:** `data/local/db/AppDatabase.kt`

```kotlin
@Database(
    entities = [
        MediaEntity::class,
        FavoriteEntity::class,
        // NOUVELLES
        WatchHistoryEntity::class,
        MarkerEntity::class,
        ChapterEntity::class,
        UserRatingEntity::class,
        UserLabelEntity::class,
        ContinueWatchingEntity::class
    ],
    version = 2, // Migration!
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun favoriteDao(): FavoriteDao
    // NOUVEAUX
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun markerDao(): MarkerDao
    abstract fun chapterDao(): ChapterDao
    abstract fun userRatingDao(): UserRatingDao
    abstract fun userLabelDao(): UserLabelDao
}
```

**Migration DB (v1→v2):**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Créer les 6 nouvelles tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS watch_history (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                type TEXT NOT NULL,
                watchedAt INTEGER NOT NULL,
                ...
            )
        """)
        // ... (autres CREATE TABLE)
    }
}
```

---

### 📦 PHASE 2 : COUCHE REPOSITORY (3-4 jours)

#### **Tâche 2.1 : Étendre MediaRepository**
**Fichier:** `domain/repository/MediaRepository.kt`

```kotlin
class MediaRepository(
    private val movieApiService: MovieApiService,
    private val mediaDao: MediaDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val continueWatchingDao: ContinueWatchingDao,
    private val markerDao: MarkerDao,
    private val chapterDao: ChapterDao
) {
    // Nouveaux appels API
    fun getRecentlyAdded(limit: Int = 50): Flow<Result<List<MediaDetail>>> = flow {
        emit(Result.loading())
        try {
            val data = movieApiService.getRecentlyAdded(limit)
            // Cache local optionnel
            emit(Result.success(data))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
    
    fun getHubs(limit: Int = 10): Flow<Result<Map<String, List<MediaDetail>>>> = flow {
        emit(Result.loading())
        try {
            val hubs = movieApiService.getHubs(limit)
            emit(Result.success(hubs))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
    
    fun getContinueWatching(): Flow<Result<List<MediaDetail>>> = flow {
        emit(Result.loading())
        try {
            val items = movieApiService.getContinueWatching()
            // Sauvegarder dans cache local
            items.forEach { media ->
                val entity = ContinueWatchingEntity(
                    mediaId = media.id,
                    title = media.title,
                    type = media.type,
                    posterUrl = media.posterUrl,
                    viewOffset = media.viewOffset,
                    duration = media.sources.firstOrNull()?.streamUrl?.let { /* parse */ } ?: 0,
                    progressPercent = if (media.duration > 0) 
                        (media.viewOffset * 100) / media.duration else 0f,
                    lastViewedAt = System.currentTimeMillis()
                )
                continueWatchingDao.insert(entity)
            }
            emit(Result.success(items))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
    
    fun getWatchHistory(): Flow<Result<List<HistoryEntry>>> = flow {
        emit(Result.loading())
        try {
            val history = movieApiService.getWatchHistory()
            // Cache
            history.forEach { entry ->
                watchHistoryDao.insert(entry.toEntity())
            }
            emit(Result.success(history))
        } catch (e: Exception) {
            // Fallback cache local
            val cached = watchHistoryDao.getAllHistory()
            if (cached.isNotEmpty()) {
                emit(Result.success(cached.map { it.toDomain() }))
            } else {
                emit(Result.error(e))
            }
        }
    }
    
    fun advancedSearch(
        title: String? = null,
        year: Int? = null,
        unwatched: Boolean? = null,
        limit: Int = 50
    ): Flow<Result<List<MediaDetail>>> = flow {
        emit(Result.loading())
        try {
            val results = movieApiService.advancedSearch(title, year, unwatched, limit)
            emit(Result.success(results))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
    
    fun getNowPlaying(): Flow<Result<List<SessionInfo>>> = flow {
        emit(Result.loading())
        try {
            val sessions = movieApiService.getNowPlaying()
            emit(Result.success(sessions))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
}
```

---

#### **Tâche 2.2 : Créer PlaybackRepository** (NOUVEAU)
**Fichier:** `domain/repository/PlaybackRepository.kt`

```kotlin
class PlaybackRepository(
    private val movieApiService: MovieApiService,
    private val markerDao: MarkerDao,
    private val chapterDao: ChapterDao,
    private val userRatingDao: UserRatingDao,
    private val userLabelDao: UserLabelDao
) {
    // Marquer comme vu/non-vu
    suspend fun markWatched(mediaId: String, watched: Boolean): Result<Unit> = try {
        movieApiService.markWatched(ScrobbleRequest(key = mediaId, action = if (watched) "watched" else "unwatched"))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
    
    // Mettre à jour progression
    suspend fun updateProgress(mediaId: String, timeMs: Long): Result<Unit> = try {
        movieApiService.updateProgress(ProgressRequest(key = mediaId, time_ms = timeMs))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
    
    // Ajouter aux favoris
    suspend fun addToFavorites(mediaId: String): Result<Unit> = try {
        movieApiService.addFavorite(mediaId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
    
    // Noter un média
    suspend fun rateMedia(mediaId: String, rating: Float): Result<Unit> = try {
        require(rating in 0f..10f) { "Rating must be between 0 and 10" }
        movieApiService.rateMedia(mediaId, rating)
        userRatingDao.upsertRating(UserRatingEntity(mediaId, rating, System.currentTimeMillis()))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
    
    // Obtenir la note locale
    suspend fun getLocalRating(mediaId: String): Float? = 
        userRatingDao.getRating(mediaId)
    
    // Ajouter/Supprimer label
    suspend fun addLabel(mediaId: String, label: String): Result<Unit> = try {
        movieApiService.addLabel(mediaId, label)
        userLabelDao.addLabel(UserLabelEntity(mediaId = mediaId, labelName = label))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
    
    suspend fun removeLabel(mediaId: String, label: String): Result<Unit> = try {
        movieApiService.removeLabel(mediaId, label)
        userLabelDao.removeLabel(UserLabelEntity(mediaId = mediaId, labelName = label))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error(e)
    }
    
    fun getLabels(mediaId: String): Flow<List<String>> = 
        userLabelDao.getLabels(mediaId)
    
    // Markers (intro/credits)
    fun getMarkersForMedia(mediaId: String): Flow<List<MarkerEntity>> = 
        markerDao.getMarkersForMedia(mediaId)
    
    // Chapters
    fun getChaptersForMedia(mediaId: String): Flow<List<ChapterEntity>> = 
        chapterDao.getChaptersForMedia(mediaId)
    
    // Cache markers depuis MediaDetail
    suspend fun cacheMarkersForMedia(mediaId: String, markers: List<Marker>) {
        val entities = markers.map { marker ->
            MarkerEntity(
                mediaId = mediaId,
                title = marker.title,
                type = marker.type,
                startTimeMs = marker.start_time,
                endTimeMs = marker.end_time
            )
        }
        markerDao.insertAll(entities)
    }
}
```

---

### 🎨 PHASE 3 : VIEWMODELS & LOGIQUE MÉTIER (4-5 jours)

#### **Tâche 3.1 : Étendre HomeViewModel**
**Fichier:** `ui/screens/HomeViewModel.kt`

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playbackRepository: PlaybackRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // EXISTANT
    val moviesState: StateFlow<PagingData<MediaDetail>> = ...
    
    // NOUVEAUX
    private val _recentlyAddedState = MutableStateFlow<Result<List<MediaDetail>>>(Result.loading())
    val recentlyAddedState: StateFlow<Result<List<MediaDetail>>> = _recentlyAddedState.asStateFlow()
    
    private val _hubsState = MutableStateFlow<Result<Map<String, List<MediaDetail>>>>(Result.loading())
    val hubsState: StateFlow<Result<Map<String, List<MediaDetail>>>> = _hubsState.asStateFlow()
    
    private val _continueWatchingState = MutableStateFlow<Result<List<MediaDetail>>>(Result.loading())
    val continueWatchingState: StateFlow<Result<List<MediaDetail>>> = _continueWatchingState.asStateFlow()
    
    private val _sortOption = MutableStateFlow("added_at")
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            // Load hubs
            mediaRepository.getHubs(10).collect { result ->
                _hubsState.value = result
            }
            
            // Load recently added
            mediaRepository.getRecentlyAdded(20).collect { result ->
                _recentlyAddedState.value = result
            }
            
            // Load continue watching
            mediaRepository.getContinueWatching().collect { result ->
                _continueWatchingState.value = result
            }
        }
    }
    
    fun setSortOption(option: String) {
        _sortOption.value = option
        // Trigger refetch avec nouveau tri
        loadHomeData()
    }
    
    fun refreshHomeData() {
        loadHomeData()
    }
}
```

---

#### **Tâche 3.2 : Créer DetailViewModel Enhanced**
**Fichier:** `ui/screens/DetailViewModel.kt` (modifications)

```kotlin
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val playbackRepository: PlaybackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val mediaId: String = savedStateHandle.get<String>("mediaId") ?: ""
    
    private val _mediaDetail = MutableStateFlow<Result<MediaDetail>>(Result.loading())
    val mediaDetail: StateFlow<Result<MediaDetail>> = _mediaDetail.asStateFlow()
    
    // NOUVEAUX
    private val _markers = MutableStateFlow<List<MarkerEntity>>(emptyList())
    val markers: StateFlow<List<MarkerEntity>> = _markers.asStateFlow()
    
    private val _chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    val chapters: StateFlow<List<ChapterEntity>> = _chapters.asStateFlow()
    
    private val _userRating = MutableStateFlow<Float?>(null)
    val userRating: StateFlow<Float?> = _userRating.asStateFlow()
    
    private val _userLabels = MutableStateFlow<List<String>>(emptyList())
    val userLabels: StateFlow<List<String>> = _userLabels.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    init {
        loadMediaDetail()
        loadAdditionalData()
    }
    
    private fun loadMediaDetail() {
        viewModelScope.launch {
            mediaRepository.getMediaDetail(mediaId).collect { result ->
                _mediaDetail.value = result
                // Cache markers & chapters
                result.data?.let { media ->
                    playbackRepository.cacheMarkersForMedia(mediaId, media.markers)
                }
            }
        }
    }
    
    private fun loadAdditionalData() {
        viewModelScope.launch {
            // Load markers
            playbackRepository.getMarkersForMedia(mediaId).collect { markers ->
                _markers.value = markers
            }
            
            // Load chapters
            playbackRepository.getChaptersForMedia(mediaId).collect { chapters ->
                _chapters.value = chapters
            }
            
            // Load user rating
            val rating = playbackRepository.getLocalRating(mediaId)
            _userRating.value = rating
            
            // Load labels
            playbackRepository.getLabels(mediaId).collect { labels ->
                _userLabels.value = labels
            }
        }
    }
    
    fun addToFavorites() {
        viewModelScope.launch {
            val result = playbackRepository.addToFavorites(mediaId)
            if (result is Result.Success) {
                _isFavorite.value = true
            }
        }
    }
    
    fun rateMedia(rating: Float) {
        viewModelScope.launch {
            val result = playbackRepository.rateMedia(mediaId, rating)
            if (result is Result.Success) {
                _userRating.value = rating
            }
        }
    }
    
    fun addLabel(label: String) {
        viewModelScope.launch {
            playbackRepository.addLabel(mediaId, label)
        }
    }
    
    fun removeLabel(label: String) {
        viewModelScope.launch {
            playbackRepository.removeLabel(mediaId, label)
        }
    }
}
```

---

#### **Tâche 3.3 : Créer PlayerViewModel Enhanced**
**Fichier:** `ui/screens/PlayerViewModel.kt` (modifications)

```kotlin
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackRepository: PlaybackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val mediaId: String = savedStateHandle.get<String>("mediaId") ?: ""
    
    // NOUVEAUX
    private val _markers = MutableStateFlow<List<MarkerEntity>>(emptyList())
    val markers: StateFlow<List<MarkerEntity>> = _markers.asStateFlow()
    
    private val _chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    val chapters: StateFlow<List<ChapterEntity>> = _chapters.asStateFlow()
    
    private val _currentChapter = MutableStateFlow<ChapterEntity?>(null)
    val currentChapter: StateFlow<ChapterEntity?> = _currentChapter.asStateFlow()
    
    private val _nextMarkerPosition = MutableStateFlow<Int?>(null)
    val nextMarkerPosition: StateFlow<Int?> = _nextMarkerPosition.asStateFlow()
    
    init {
        loadPlaybackData()
    }
    
    private fun loadPlaybackData() {
        viewModelScope.launch {
            playbackRepository.getMarkersForMedia(mediaId).collect { markers ->
                _markers.value = markers
            }
            
            playbackRepository.getChaptersForMedia(mediaId).collect { chapters ->
                _chapters.value = chapters
            }
        }
    }
    
    fun updatePlaybackProgress(timeMs: Long) {
        viewModelScope.launch {
            playbackRepository.updateProgress(mediaId, timeMs)
            
            // Update current chapter
            _currentChapter.value = _chapters.value.firstOrNull { chapter ->
                timeMs >= chapter.startTimeMs && timeMs <= chapter.endTimeMs
            }
            
            // Update next marker
            _nextMarkerPosition.value = _markers.value.firstOrNull { marker ->
                marker.startTimeMs > timeMs
            }?.startTimeMs
        }
    }
    
    fun markMediaWatched() {
        viewModelScope.launch {
            playbackRepository.markWatched(mediaId, watched = true)
        }
    }
    
    fun shouldSkipIntro(currentTimeMs: Long): Boolean {
        val introMarker = _markers.value.firstOrNull { it.type == "intro" }
        return introMarker != null && currentTimeMs >= introMarker.startTimeMs && currentTimeMs < introMarker.endTimeMs
    }
    
    fun shouldSkipCredits(currentTimeMs: Long, totalDuration: Long): Boolean {
        val creditsMarker = _markers.value.firstOrNull { it.type == "credits" }
        return creditsMarker != null && currentTimeMs >= creditsMarker.startTimeMs && currentTimeMs <= creditsMarker.endTimeMs
    }
}
```

---

#### **Tâche 3.4 : Créer HistoryViewModel** (NOUVEAU)
**Fichier:** `ui/screens/history/HistoryViewModel.kt`

```kotlin
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {
    
    private val _watchHistory = MutableStateFlow<Result<List<HistoryEntry>>>(Result.loading())
    val watchHistory: StateFlow<Result<List<HistoryEntry>>> = _watchHistory.asStateFlow()
    
    init {
        loadWatchHistory()
    }
    
    fun loadWatchHistory() {
        viewModelScope.launch {
            mediaRepository.getWatchHistory().collect { result ->
                _watchHistory.value = result
            }
        }
    }
}
```

---

### 🖼️ PHASE 4 : UI COMPOSABLES (5-6 jours)

#### **Tâche 4.1 : Nouveau composable "HubsSection"**
**Fichier:** `ui/components/HubsSection.kt` (NOUVEAU)

```kotlin
@Composable
fun HubsSection(
    hubs: Map<String, List<MediaDetail>>,
    onMediaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        hubs.forEach { (hubName, items) ->
            Text(
                text = hubName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items.size) { index ->
                    val media = items[index]
                    MediaCard(
                        media = media,
                        onClick = { onMediaClick(media.id) },
                        modifier = Modifier
                            .width(300.dp)
                            .height(400.dp)
                    )
                }
            }
        }
    }
}
```

---

#### **Tâche 4.2 : Nouveau composable "ContinueWatchingCarousel"**
**Fichier:** `ui/components/ContinueWatchingCarousel.kt` (NOUVEAU)

```kotlin
@Composable
fun ContinueWatchingCarousel(
    items: List<MediaDetail>,
    onMediaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Reprendre la lecture",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items.size) { index ->
                val media = items[index]
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .height(240.dp)
                        .clickable { onMediaClick(media.id) }
                ) {
                    AsyncImage(
                        model = media.posterUrl,
                        contentDescription = media.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Progress bar overlay
                    LinearProgressIndicator(
                        progress = (media.viewOffset.toFloat() / media.sources.firstOrNull()?.duration?.toFloat() ?: 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter),
                        color = Color(0xFFFF6B35)
                    )
                }
            }
        }
    }
}
```

---

#### **Tâche 4.3 : Nouveau écran "HistoryScreen"**
**Fichier:** `ui/screens/history/HistoryScreen.kt` (NOUVEAU)

```kotlin
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val watchHistory by viewModel.watchHistory.collectAsState()
    
    when (watchHistory) {
        is Result.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Result.Success -> {
            val history = (watchHistory as Result.Success).data
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history.size) { index ->
                    val entry = history[index]
                    HistoryEntryCard(
                        entry = entry,
                        onClick = { onNavigateToDetail(entry.id) }
                    )
                }
            }
        }
        is Result.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Erreur lors du chargement de l'historique")
            }
        }
    }
}

@Composable
fun HistoryEntryCard(
    entry: HistoryEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AsyncImage(
                model = entry.thumbUrl,
                contentDescription = entry.title,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(entry.title, fontWeight = FontWeight.Bold)
                Text("${entry.type} • ${formatDate(entry.watchedAt)}", fontSize = 12.sp)
                Text("${entry.viewOffset / 1000}s / ${entry.duration / 1000}s", fontSize = 10.sp)
            }
        }
    }
}
```

---

#### **Tâche 4.4 : PlayerScreen avec Markers & Chapters**
**Fichier:** `ui/screens/PlayerScreen.kt` (modifications)

```kotlin
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    mediaId: String
) {
    val markers by viewModel.markers.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    
    var currentTime by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) }
    
    // ExoPlayer instance
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Lecteur video
        AndroidView(
            factory = { ctx ->
                StyledPlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay: Current Chapter Info
        if (currentChapter != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
            ) {
                Text(
                    text = "Chapitre: ${currentChapter!!.title}",
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        
        // Timeline avec markers
        PlayerTimeline(
            totalDuration = player.duration,
            currentPosition = currentTime,
            chapters = chapters,
            markers = markers,
            onSeek = { newTime ->
                player.seekTo(newTime)
                currentTime = newTime
                viewModel.updatePlaybackProgress(newTime)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
        )
    }
}

@Composable
fun PlayerTimeline(
    totalDuration: Long,
    currentPosition: Long,
    chapters: List<ChapterEntity>,
    markers: List<MarkerEntity>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            
            // Draw chapter markers
            chapters.forEach { chapter ->
                val x = (chapter.startTimeMs.toFloat() / totalDuration) * width
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2f
                )
            }
            
            // Draw intro/credits markers
            markers.forEach { marker ->
                val startX = (marker.startTimeMs.toFloat() / totalDuration) * width
                val endX = (marker.endTimeMs.toFloat() / totalDuration) * width
                val color = if (marker.type == "intro") Color(0xFFFF9800) else Color(0xFF673AB7)
                drawRect(
                    color = color.copy(alpha = 0.5f),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, size.height)
                )
            }
            
            // Progress bar
            val progressX = (currentPosition.toFloat() / totalDuration) * width
            drawLine(
                color = Color.Red,
                start = Offset(progressX, 0f),
                end = Offset(progressX, size.height),
                strokeWidth = 3f
            )
        }
        
        // Clickable area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newPosition = (offset.x / size.width) * totalDuration
                        onSeek(newPosition.toLong())
                    }
                }
        )
    }
}
```

---

#### **Tâche 4.5 : DetailScreen avec Rating & Labels**
**Fichier:** `ui/screens/DetailScreen.kt` (modifications)

```kotlin
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onPlayClick: () -> Unit
) {
    val mediaDetail by viewModel.mediaDetail.collectAsState()
    val userRating by viewModel.userRating.collectAsState()
    val userLabels by viewModel.userLabels.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        when (mediaDetail) {
            is Result.Success -> {
                val media = (mediaDetail as Result.Success).data
                
                // Poster + Info
                Row {
                    AsyncImage(
                        model = media.posterUrl,
                        contentDescription = media.title,
                        modifier = Modifier
                            .width(200.dp)
                            .height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(media.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("${media.year} • ${media.genres.joinToString(", ")}")
                        
                        // Rating stars
                        RatingBar(
                            rating = userRating ?: 0f,
                            onRatingChange = { viewModel.rateMedia(it) },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(onClick = onPlayClick) {
                            Text("Lire")
                        }
                    }
                }
                
                // Summary
                Text("Synopsis", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp))
                Text(media.summary)
                
                // Labels/Tags
                if (userLabels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Mes étiquettes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        userLabels.forEach { label ->
                            Chip(
                                label = { Text(label) },
                                onDismiss = { viewModel.removeLabel(label) }
                            )
                        }
                    }
                }
                
                // Markers info
                if (media.markers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Marqueurs spéciaux", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    media.markers.forEach { marker ->
                        Text("• ${marker.title} (${formatMs(marker.start_time)} - ${formatMs(marker.end_time)})")
                    }
                }
                
                // Chapters info
                if (media.chapters.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chapitres", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    media.chapters.forEach { chapter ->
                        Text("• ${chapter.title}")
                    }
                }
            }
            is Result.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Result.Error -> {
                Text("Erreur de chargement")
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.height(32.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(10) { index ->
            val starRating = index + 1
            Icon(
                imageVector = if (starRating <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Étoile $starRating",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onRatingChange(starRating.toFloat()) }
                    .padding(4.dp),
                tint = if (starRating <= rating) Color(0xFFFFB800) else Color.Gray
            )
        }
    }
}
```

---

#### **Tâche 4.6 : Mettre à jour HomeScreen**
**Fichier:** `ui/screens/HomeScreen.kt` (modifications)

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val recentlyAdded by viewModel.recentlyAddedState.collectAsState()
    val hubs by viewModel.hubsState.collectAsState()
    val continueWatching by viewModel.continueWatchingState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        // 1. Continue Watching
        if (continueWatching is Result.Success) {
            ContinueWatchingCarousel(
                items = (continueWatching as Result.Success).data,
                onMediaClick = onNavigateToDetail
            )
        }
        
        // 2. Hubs (Recommended)
        if (hubs is Result.Success) {
            HubsSection(
                hubs = (hubs as Result.Success).data,
                onMediaClick = onNavigateToDetail
            )
        }
        
        // 3. Recently Added
        if (recentlyAdded is Result.Success) {
            Text(
                text = "Récemment Ajoutés",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp, 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items((recentlyAdded as Result.Success).data.size) { index ->
                    val media = (recentlyAdded as Result.Success).data[index]
                    MediaCard(
                        media = media,
                        onClick = { onNavigateToDetail(media.id) }
                    )
                }
            }
        }
    }
}
```

---

### 🔄 PHASE 5 : SYNCHRONISATION & WORKERS (2-3 jours)

#### **Tâche 5.1 : Créer SyncWorker pour refresh périodique**
**Fichier:** `worker/SyncWorker.kt` (modifications)

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    @Inject
    lateinit var mediaRepository: MediaRepository
    
    override suspend fun doWork(): Result = try {
        // Sync recently added
        mediaRepository.getRecentlyAdded(50).collect { _ -> }
        
        // Sync hubs
        mediaRepository.getHubs(10).collect { _ -> }
        
        // Sync continue watching
        mediaRepository.getContinueWatching().collect { _ -> }
        
        // Sync watch history
        mediaRepository.getWatchHistory().collect { _ -> }
        
        Result.success()
    } catch (e: Exception) {
        Log.e("SyncWorker", "Sync failed", e)
        Result.retry()
    }
    
    companion object {
        fun schedule(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                6, TimeUnit.HOURS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "media_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
```

---

## <a name="sprints"></a>4️⃣ SPRINTS RECOMMANDÉS

### **Sprint 1 (Semaine 1) : Fondations API & DB**
- **Durée:** 5 jours (40h)
- **Tâches:**
  - 1.1 : Étendre MovieApiService (4h)
  - 1.2 : Créer tables Room (6h)
  - 1.3 : Migration DB (3h)
  - 2.1 : Étendre MediaRepository (6h)
  - 2.2 : Créer PlaybackRepository (5h)
  - **Tests unitaires:** Repositories (8h)
  - **Buffers & Ajustements:** (3h)

**Sortie :** Couche données complète + tests ✅

---

### **Sprint 2 (Semaine 2) : ViewModels & Logique Métier**
- **Durée:** 5 jours (40h)
- **Tâches:**
  - 3.1 : Étendre HomeViewModel (4h)
  - 3.2 : DetailViewModel Enhanced (5h)
  - 3.3 : PlayerViewModel Enhanced (6h)
  - 3.4 : HistoryViewModel (3h)
  - **Tests Coroutines/State:** (8h)
  - **Documentation ViewModels:** (5h)
  - **Debugging & Ajustements:** (4h)

**Sortie :** Logic layer complète + tests ✅

---

### **Sprint 3 (Semaine 3) : UI Composables & Navigation**
- **Durée:** 6 jours (48h)
- **Tâches:**
  - 4.1 : HubsSection (4h)
  - 4.2 : ContinueWatchingCarousel (4h)
  - 4.3 : HistoryScreen (5h)
  - 4.4 : PlayerScreen Markers/Chapters (8h)
  - 4.5 : DetailScreen Rating/Labels (6h)
  - 4.6 : HomeScreen Updates (5h)
  - **Tests UI Composables:** (8h)
  - **TV-Specific Testing (D-pad, Focus):** (4h)

**Sortie :** UI complète + tests ✅

---

### **Sprint 4 (Semaine 4) : Synchronisation & Polish**
- **Durée:** 5 jours (40h)
- **Tâches:**
  - 5.1 : SyncWorker (3h)
  - **Error Handling & Retry Logic:** (6h)
  - **Performance Optimization:** (6h)
  - **Image Proxy & Caching:** (4h)
  - **TV Compatibility Pass:** (8h)
  - **E2E Tests:** (5h)
  - **Buffers & Hotfixes:** (3h)

**Sortie :** Produit complet & stable ✅

---

## <a name="dépendances"></a>5️⃣ DÉPENDANCES & INTÉGRATIONS

### ✅ À Conserver (Existant)

- Retrofit / OkHttp
- Room Database
- Jetpack Compose (Material3)
- ExoPlayer
- Hilt DI
- Coroutines + Flow
- Coil (Images)

### ✨ Dépendances Nouvelles Recommandées

```kotlin
// Proxy Image Optimization (Optional)
implementation("io.coil-kt:coil-compose:2.4.0")

// Advanced Player UI (Markers Timeline)
implementation("androidx.media3:media3-ui:1.1.0")

// WorkManager pour SyncWorker
implementation("androidx.work:work-runtime-ktx:2.8.1")

// Paging (déjà présent ?)
implementation("androidx.paging:paging-compose:3.2.0")

// DataStore pour cache preferences
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
```

---

## <a name="risques"></a>6️⃣ RISQUES & MITIGATIONS

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|-----------|
| **API Timeout** sur /api/hubs (réseau lent) | MOYENNE | MOYEN | Timeout retry + cache local 5min |
| **DB Migration** échoue v1→v2 | BASSE | CRITIQUE | Tests migration + backup avant déploiement |
| **Markers/Chapters** pas présent dans source API | BASSE | MOYEN | Fallback : afficher UI sans markers |
| **ExoPlayer crash** avec markers timeline | BASSE | MOYEN | Try-catch autour Canvas, tests TV device |
| **Performance:** Loading trop lent sur Home | MOYENNE | MOYEN | Pagination Hubs, lazy loading composables |
| **D-pad Navigation** sur nouveaux écrans | MOYENNE | MOYEN | Tests TV + FocusManager tweaks |
| **Sync Worker** drain batterie | BASSE | MOYEN | Polling 6h → check network state |

**Mitigation Globale:**
- ✅ Tests unitaires pour chaque Repository
- ✅ Tests Composable pour UI (Compose test framework)
- ✅ Tests E2E sur émulateur TV
- ✅ Monitoring logs + Crashlytics
- ✅ Phased rollout (beta testers)

---

## 📊 RÉSUMÉ EFFORT

| Phase | Durée | Effort | Ressources |
|-------|-------|--------|-----------|
| **P1: Infrastructure** | 3j | 40h | 1 Dev Backend + 1 Senior (review) |
| **P2: ViewModels** | 5j | 40h | 1 Dev Kotlin/MVVM |
| **P3: UI** | 6j | 48h | 1 Dev Compose + 1 Designer TV |
| **P4: Polish & Tests** | 5j | 40h | 1 QA + 1 Dev |
| **TOTAL** | **20 jours** | **168h** | **3-4 devs** |

---

## 🎯 LIVRABLES PAR SPRINT

**Sprint 1 ✅**
- [ ] MovieApiService complète
- [ ] Room DB migrations
- [ ] MediaRepository + PlaybackRepository
- [ ] Tests 80%+ couverture

**Sprint 2 ✅**
- [ ] HomeViewModel, DetailViewModel, PlayerViewModel, HistoryViewModel
- [ ] Coroutines + State Flow tests
- [ ] Documentation API interne

**Sprint 3 ✅**
- [ ] HubsSection, ContinueWatchingCarousel
- [ ] HistoryScreen, PlayerScreen Markers, DetailScreen Rating
- [ ] HomeScreen integration
- [ ] TV D-pad tests

**Sprint 4 ✅**
- [ ] SyncWorker
- [ ] Performance profiles
- [ ] E2E test suite
- [ ] Production-ready APK

---

**Generated:** 19 Janvier 2026 | **Version:** 1.0 | **Status:** READY FOR IMPLEMENTATION ✅

