# 🏗️ PROJECT STRUCTURE COMPLETE

## Architecture Complète du Projet

Guide de référence pour comprendre toute l'architecture.

---

## 📁 FOLDER STRUCTURE

```
android-tv-plex/
├── backend/                          # Python Flask Backend
│   ├── main.py                      # Entry point
│   ├── requirements.txt              # Dependencies
│   ├── image_cache.py                # Cache management
│   ├── venv/                         # Virtual environment
│   └── image_cache/                  # Cache folder
│
├── android/                          # Android TV App
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── kotlin/
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── screens/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   ├── DetailScreen.kt
│   │   │   │   │   │   │   ├── FavoritesScreen.kt
│   │   │   │   │   │   │   └── SearchScreen.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── MovieCard.kt
│   │   │   │   │   │       ├── MovieRow.kt
│   │   │   │   │   │       └── NavigationBar.kt
│   │   │   │   │   │
│   │   │   │   │   ├── viewmodel/
│   │   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   │   ├── DetailViewModel.kt
│   │   │   │   │   │   ├── FavoritesViewModel.kt
│   │   │   │   │   │   └── SearchViewModel.kt
│   │   │   │   │   │
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── local/
│   │   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   │   ├── MovieDao.kt
│   │   │   │   │   │   │   │   ├── FavoriteDao.kt
│   │   │   │   │   │   │   │   └── PlayHistoryDao.kt
│   │   │   │   │   │   │   └── entity/
│   │   │   │   │   │   │       ├── MovieEntity.kt
│   │   │   │   │   │   │       ├── FavoriteEntity.kt
│   │   │   │   │   │   │       └── PlayHistoryEntity.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── remote/
│   │   │   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   │   │   └── dto/
│   │   │   │   │   │   │       ├── Collection.kt
│   │   │   │   │   │   │       └── Movie.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── repository/
│   │   │   │   │   │       ├── HomeRepository.kt
│   │   │   │   │   │       ├── FavoritesRepository.kt
│   │   │   │   │   │       └── PlaybackRepository.kt
│   │   │   │   │   │
│   │   │   │   │   └── di/
│   │   │   │   │       ├── NetworkModule.kt
│   │   │   │   │       ├── DatabaseModule.kt
│   │   │   │   │       ├── PreferencesModule.kt
│   │   │   │   │       └── RepositoryModule.kt
│   │   │   │   │
│   │   │   │   └── AndroidManifest.xml
│   │   │   │
│   │   │   ├── test/                # Unit Tests
│   │   │   └── androidTest/         # Integration Tests
│   │   │
│   │   ├── build.gradle
│   │   └── proguard-rules.pro
│   │
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradle/
│
└── docs/                            # Documentation
    ├── README.md
    ├── ARCHITECTURE.md
    ├── API_DOCS.md
    └── DEPLOYMENT.md
```

---

## 🔄 DATA FLOW

```
┌──────────────────────────────────────────────────────┐
│                  PLEX SERVER                         │
│         (Collections, Movies, Images)                │
└─────────────────────┬────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────────────┐
│           PYTHON FLASK BACKEND (8000)                │
│  ├─ /api/collections  → List of collections         │
│  ├─ /api/collections/trending → Trending movies     │
│  ├─ /api/collections/new → New releases             │
│  ├─ /api/collections/continue → Continue watching   │
│  └─ /api/proxy/image → Image proxy (WebP cache)     │
└─────────────────┬──────────────────┬─────────────────┘
                  │                  │
                  ▼                  ▼
          HTTP Requests        Image Cache
                  │                  │
                  ▼                  ▼
┌──────────────────────────────────────────────────────┐
│         ANDROID TV APP (Retrofit/Coil)               │
│                                                      │
│  ApiService ──→ Repository ──→ ViewModel ──→ UI    │
│                                                      │
│  Cache:                                              │
│  ├─ Room Database (Favorites, PlayHistory)          │
│  ├─ Preferences (User settings)                     │
│  └─ Image Cache (WebP images)                       │
└──────────────────────────────────────────────────────┘
```

---

## 📱 SCREEN NAVIGATION

```
MainActivity
  ├─ HomeScreen (Default)
  │   ├─ Trending Section (horizontal scroll)
  │   ├─ New Releases Section (horizontal scroll)
  │   ├─ Continue Watching Section (horizontal scroll)
  │   └─ Click → DetailScreen
  │
  ├─ DetailScreen
  │   ├─ Movie poster (full width)
  │   ├─ Title, rating, year
  │   ├─ Play button → PlaybackScreen
  │   ├─ Favorite button → Toggle
  │   └─ Summary text
  │
  ├─ FavoritesScreen
  │   ├─ List of favorite movies
  │   ├─ Remove button
  │   └─ Click → DetailScreen
  │
  └─ SearchScreen
      ├─ Search bar
      ├─ Results list
      └─ Click → DetailScreen
```

---

## 🗄️ DATABASE SCHEMA

### Movies Table
```sql
CREATE TABLE movies (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    poster TEXT,
    year INTEGER,
    rating REAL,
    summary TEXT,
    last_updated INTEGER
);
```

### Favorites Table
```sql
CREATE TABLE favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_id TEXT NOT NULL UNIQUE,
    title TEXT,
    poster TEXT,
    added_at INTEGER
);
```

### Play History Table
```sql
CREATE TABLE play_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_id TEXT NOT NULL UNIQUE,
    position INTEGER,
    duration INTEGER,
    last_played INTEGER
);
```

---

## 🔌 API ENDPOINTS

| Endpoint | Method | Response |
|----------|--------|----------|
| `/health` | GET | Health status |
| `/api/collections` | GET | List of collections |
| `/api/collections/trending` | GET | Trending movies |
| `/api/collections/new` | GET | New releases |
| `/api/collections/continue` | GET | Continue watching |
| `/api/proxy/image/{key}` | GET | Image proxy (WebP) |

---

## 🎯 KEY COMPONENTS

### Backend (Python)
- Flask app with CORS
- Plex API integration
- Image proxy with WebP conversion
- Image caching system

### Android
- Jetpack Compose UI
- Retrofit HTTP client
- Room database for offline data
- ViewModel for state management
- Coil for image loading
- Hilt for dependency injection

---

## 🔐 SECURITY

- API calls over HTTP (upgrade to HTTPS in production)
- Plex token stored in backend only
- User data in Room database (local, encrypted possible)
- No sensitive data in logs

---

## 📊 DEPENDENCIES

**Backend:**
- Flask 2.3.x
- Pillow (PIL) 9.x
- Requests 2.x

**Android:**
- Retrofit 2.9.x
- Coil 2.4.x
- Room 2.5.x
- Jetpack Compose 1.5.x
- Hilt 2.46.x
- Coroutines 1.7.x

---

**Architecture: MVVM + Repository Pattern = Clean, Testable, Maintainable** ✅
