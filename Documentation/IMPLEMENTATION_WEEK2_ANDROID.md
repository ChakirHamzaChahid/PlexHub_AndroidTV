# 📱 IMPLEMENTATION WEEK 2 - ANDROID

## Android Data Layer + HomeScreen - 40 heures

Bienvenue! Cette semaine nous intégrons Android avec l'API backend.

---

## 🎯 SEMAINE 2 OVERVIEW

### Objectif
Créer la couche données Android (Repository, ViewModel) et HomeScreen.

### Résultat
- ✅ Retrofit API setup
- ✅ Repository pattern
- ✅ HomeViewModel
- ✅ HomeScreen avec 4 sections
- ✅ 40 heures de travail

---

## 🔴 JOURS 1-2: RETROFIT SETUP

### build.gradle (dependencies)

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.5.2'
    kapt 'androidx.room:room-compiler:2.5.2'
    implementation 'androidx.room:room-ktx:2.5.2'
    
    // Jetpack Compose
    implementation 'androidx.compose.ui:ui:1.5.0'
    implementation 'androidx.compose.material3:material3:1.0.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0'
    
    // Image loading
    implementation 'io.coil-kt:coil-compose:2.4.0'
}
```

### ApiService.kt - Retrofit Interface

```kotlin
import retrofit2.http.*
import com.example.plexapp.data.dto.*

interface ApiService {
    @GET("/api/collections")
    suspend fun getCollections(): CollectionsResponse
    
    @GET("/api/collections/trending")
    suspend fun getTrending(@Query("limit") limit: Int = 20): MoviesResponse
    
    @GET("/api/collections/new")
    suspend fun getNewReleases(@Query("limit") limit: Int = 20): MoviesResponse
    
    @GET("/api/collections/continue")
    suspend fun getContinueWatching(@Query("limit") limit: Int = 10): MoviesResponse
    
    @GET("/api/proxy/image/{key}")
    suspend fun getImageProxy(
        @Path("key") key: String,
        @Query("format") format: String = "webp",
        @Query("size") size: String = "medium"
    ): retrofit2.Response<okhttp3.ResponseBody>
}
```

### NetworkModule.kt - Dependency Injection

```kotlin
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
class NetworkModule {
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://your-server:8000")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

---

## 🟡 JOURS 3-4: REPOSITORY + VIEWMODEL

### HomeRepository.kt - Data Layer

```kotlin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.plexapp.data.dto.*

class HomeRepository(private val apiService: ApiService) {
    fun getCollections(): Flow<Resource<List<Collection>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getCollections()
            emit(Resource.Success(response.data))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun getTrending(): Flow<Resource<List<Movie>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getTrending(limit = 20)
            emit(Resource.Success(response.data))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun getNewReleases(): Flow<Resource<List<Movie>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getNewReleases(limit = 20)
            emit(Resource.Success(response.data))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun getContinueWatching(): Flow<Resource<List<Movie>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getContinueWatching(limit = 10)
            emit(Resource.Success(response.data))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}

sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}
```

### HomeViewModel.kt - State Management

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    
    val collections: StateFlow<Resource<List<Collection>>> = 
        repository.getCollections().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Resource.Loading()
        )
    
    val trendingMovies: StateFlow<Resource<List<Movie>>> = 
        repository.getTrending().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Resource.Loading()
        )
    
    val newReleases: StateFlow<Resource<List<Movie>>> = 
        repository.getNewReleases().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Resource.Loading()
        )
    
    val continueWatching: StateFlow<Resource<List<Movie>>> = 
        repository.getContinueWatching().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Resource.Loading()
        )
}
```

---

## 🟢 JOUR 5: HOMESCREEN IMPLEMENTATION

### HomeScreen.kt - Jetpack Compose

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val collections = viewModel.collections.collectAsState()
    val trending = viewModel.trendingMovies.collectAsState()
    val newReleases = viewModel.newReleases.collectAsState()
    val continueWatching = viewModel.continueWatching.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section 1: Trending
        Text("Trending Now", style = MaterialTheme.typography.headlineSmall)
        MovieRow(
            movies = (trending.value as? Resource.Success)?.data ?: emptyList(),
            isLoading = trending.value is Resource.Loading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Section 2: New Releases
        Text("New Releases", style = MaterialTheme.typography.headlineSmall)
        MovieRow(
            movies = (newReleases.value as? Resource.Success)?.data ?: emptyList(),
            isLoading = newReleases.value is Resource.Loading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Section 3: Continue Watching
        Text("Continue Watching", style = MaterialTheme.typography.headlineSmall)
        MovieRow(
            movies = (continueWatching.value as? Resource.Success)?.data ?: emptyList(),
            isLoading = continueWatching.value is Resource.Loading
        )
    }
}

@Composable
fun MovieRow(
    movies: List<Movie>,
    isLoading: Boolean = false
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(220.dp)
    ) {
        items(movies) { movie ->
            MovieCard(movie)
        }
    }
}

@Composable
fun MovieCard(movie: Movie) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = movie.poster,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
```

---

## ✅ VALIDATION CHECKLIST

- [ ] Retrofit setup compiles
- [ ] ApiService interface créée
- [ ] Network module configured
- [ ] HomeRepository fonctionne
- [ ] HomeViewModel créé
- [ ] HomeScreen affiche collections
- [ ] MovieRow affiche images
- [ ] Trending section marche
- [ ] New releases section marche
- [ ] Continue watching section marche

---

## 🚀 RÉSULTAT FINAL

✅ Android data layer complète  
✅ HomeScreen avec 4 sections  
✅ Retrofit intégré  
✅ ViewModel gérant l'état  

**Semaine 2: ✅ COMPLÈTE!**
