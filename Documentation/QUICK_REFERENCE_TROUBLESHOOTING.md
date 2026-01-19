# 🆘 QUICK REFERENCE TROUBLESHOOTING

## Guide de Dépannage Rapide

Bienvenue! Utilisez ce document si vous avez un problème.

---

## 🔴 BACKEND PROBLEMS

### ❌ Port 8000 already in use
**Erreur:** `Address already in use`

**Solution 1 (Linux/Mac):**
```bash
lsof -ti:8000 | xargs kill -9
python main.py
```

**Solution 2 (Windows):**
```bash
netstat -ano | findstr :8000
taskkill /PID <PID> /F
python main.py
```

### ❌ ModuleNotFoundError: No module named 'flask'
**Erreur:** `ModuleNotFoundError: No module named 'flask'`

**Solution:**
```bash
pip install flask flask-cors pillow requests
```

### ❌ Plex server not responding
**Erreur:** `Connection refused` ou `timeout`

**Vérifications:**
- [ ] Plex server URL correcte?
- [ ] Plex token valide?
- [ ] Firewall allow port 32400?
- [ ] Réseau accessible?

**Fix:**
```python
# Testez la connexion
import requests
url = "http://your-server:32400/library/sections"
response = requests.get(url, timeout=5)
print(response.status_code)
```

---

## 🟠 ANDROID BUILD PROBLEMS

### ❌ Gradle build fails
**Erreur:** `Build failed with exception`

**Solution:**
```bash
./gradlew clean
./gradlew build
```

### ❌ Compilation error: unresolved symbol
**Erreur:** `Unresolved symbol 'ApiService'`

**Solution:**
```bash
./gradlew build
# Ou clean et rebuild
./gradlew clean
./gradlew build
```

### ❌ Cannot connect to API
**Erreur:** `Failed to connect to backend`

**Vérifications:**
- [ ] Backend running? (`python main.py`)
- [ ] URL correcte dans ApiService?
- [ ] Port 8000 accessible?
- [ ] Localhost vs IP?

**Fix:**
```kotlin
// Use actual IP instead of localhost
const val BASE_URL = "http://192.168.1.100:8000"
```

### ❌ Images not loading
**Erreur:** Blank images ou AsyncImage error

**Solution:**
```kotlin
// Add loading placeholder
AsyncImage(
    model = imageUrl,
    contentDescription = "Image",
    contentScale = ContentScale.Crop,
    modifier = Modifier.height(220.dp),
    placeholder = painterResource(R.drawable.placeholder)
)
```

---

## 🟡 API PROBLEMS

### ❌ 404 Not Found
**Erreur:** `404 Not Found` sur `/api/collections`

**Vérifications:**
- [ ] Backend running?
- [ ] Endpoint path correct?
- [ ] Plex server responding?

**Test:**
```bash
curl http://localhost:8000/health
curl http://localhost:8000/api/collections
```

### ❌ 500 Internal Server Error
**Erreur:** `500 Internal Server Error`

**Vérifications:**
- [ ] Check backend logs
- [ ] Plex token valid?
- [ ] Network stable?
- [ ] Timeout sufficient?

**Fix:**
```python
# Add logging
import logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# Log errors
logger.error(f"Error: {str(e)}", exc_info=True)
```

### ❌ Timeout errors
**Erreur:** `Connection timeout`

**Solution:**
```python
# Increase timeout
response = requests.get(url, timeout=30)

# Or use connection pooling
session = requests.Session()
adapter = HTTPAdapter(max_retries=3)
session.mount('http://', adapter)
```

---

## 🟢 DATABASE PROBLEMS

### ❌ Room migration error
**Erreur:** `Migration not found`

**Solution:**
```kotlin
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "plex_db"
)
.fallbackToDestructiveMigration()  // ← Add this
.build()
```

### ❌ Favorite not saving
**Erreur:** Favorite disappears on restart

**Solution:**
```kotlin
// Ensure insert is called
viewModelScope.launch {
    try {
        database.favoriteDao().insert(favorite)
        Log.d("Favorite", "Saved: ${favorite.movieId}")
    } catch (e: Exception) {
        Log.e("Favorite", "Error saving", e)
    }
}
```

---

## 🔵 PERFORMANCE PROBLEMS

### ❌ Slow image loading
**Problème:** Images load très lentement

**Solutions:**
```kotlin
// 1. Use smaller images
AsyncImage(
    model = imageUrl,
    modifier = Modifier.size(150.dp)
)

// 2. Add caching
val imageLoader = ImageLoader.Builder(context)
    .memoryCache { MemoryCache(maxSizeBytes = 50 * 1024 * 1024) }
    .build()

// 3. Use WebP format
curl "http://localhost:8000/api/proxy/image/key?format=webp&size=medium"
```

### ❌ UI freezes or stutters
**Problème:** App freezes when loading

**Solutions:**
```kotlin
// Load data on background thread
viewModelScope.launch(Dispatchers.IO) {
    val data = apiService.getCollections()
    // Back to main thread for UI
    withContext(Dispatchers.Main) {
        updateUI(data)
    }
}

// Or use Flow properly
private val _movies = MutableStateFlow<List<Movie>>(emptyList())
val movies = _movies.asStateFlow()
```

### ❌ High memory usage
**Problème:** App uses too much memory

**Solutions:**
```kotlin
// Clear image cache
ImageCache.cleanup(max_age_days=7)

// Limit list items
LazyRow(modifier = Modifier.height(220.dp)) {
    items(movies.take(20)) { movie ->  // Limit to 20
        MovieCard(movie)
    }
}
```

---

## ⚫ NETWORK PROBLEMS

### ❌ SSL Certificate error
**Erreur:** `SSL_INIT_FAILED` ou `CERTIFICATE_VERIFY_FAILED`

**Solution (Development only):**
```kotlin
val client = OkHttpClient.Builder()
    .hostnameVerifier { _, _ -> true }
    .build()
```

### ❌ DNS not resolving
**Erreur:** `Unable to resolve host`

**Solution:**
```kotlin
// Use IP instead of hostname
const val BASE_URL = "http://192.168.1.100:8000"  // ✓ Good
const val BASE_URL = "http://plex-server:8000"    // ✗ May fail
```

---

## 🟣 TESTING CHECKLIST

### ✅ Before submitting to Play Store

```
Backend:
[ ] python main.py starts without errors
[ ] curl /health returns 200
[ ] /api/collections responds with data
[ ] /api/collections/trending works
[ ] /api/collections/new works
[ ] /api/collections/continue works
[ ] Images proxy converts to WebP
[ ] Cache directory created

Android:
[ ] ./gradlew build succeeds
[ ] ./gradlew test passes
[ ] App launches without crashes
[ ] HomeScreen displays collections
[ ] MovieCard images load
[ ] DetailScreen navigable
[ ] Favorites save/restore
[ ] Playback resume works
[ ] No memory leaks (Profile)

Network:
[ ] Backend reachable from device
[ ] Images load quickly
[ ] API responses < 2 seconds
[ ] Offline handling works
[ ] Error messages clear

Device:
[ ] Tested on 3+ devices
[ ] Tested on different Android versions
[ ] Tested on different screen sizes
[ ] Tested with different network speeds
[ ] Tested with low memory devices
```

---

## 📞 NEED MORE HELP?

| Document | Best For |
|----------|----------|
| IMPLEMENTATION_WEEK1_BACKEND.md | Backend problems |
| IMPLEMENTATION_WEEK2_ANDROID.md | Android build problems |
| PROJECT_STRUCTURE_COMPLETE.md | Understanding architecture |
| EXECUTIVE_SUMMARY_FINAL.md | Overall project status |

---

**Still stuck? Check the logs!**

```bash
# Backend logs
python main.py  # Watch console output

# Android logs
adb logcat | grep "Your-App-Name"

# Check specific tags
adb logcat | grep ApiService
adb logcat | grep Repository
```

Good luck! 🚀
