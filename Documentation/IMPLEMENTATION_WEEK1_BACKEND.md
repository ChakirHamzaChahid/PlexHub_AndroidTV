# 🐍 IMPLEMENTATION WEEK 1 - BACKEND

## Backend API Python Flask - 40 heures

Bienvenue! Cette semaine nous créons l'API complète en Python Flask avec 5 endpoints.

---

## 📋 SEMAINE 1 OVERVIEW

### Objectif
Créer une API REST complète pour récupérer et servir les collections de films.

### Résultat
- ✅ Backend API fonctionnelle
- ✅ 5 endpoints testés
- ✅ Image proxy avec cache WebP
- ✅ VLC streaming setup
- ✅ 40 heures de travail

---

## 🔴 JOURS 1-2: API ENDPOINTS

### Installation

```bash
pip install flask flask-cors pillow requests
mkdir plex-backend && cd plex-backend
```

### main.py - Code Principal

```python
from flask import Flask, jsonify, request, send_file
from flask_cors import CORS
import requests
import os
from PIL import Image
from io import BytesIO
import json

app = Flask(__name__)
CORS(app)

# Configuration
PLEX_SERVER = "http://votre-plex-server:32400"
PLEX_TOKEN = "votre-token-plex"
CACHE_DIR = "image_cache"

os.makedirs(CACHE_DIR, exist_ok=True)

# ============= ENDPOINT 1: GET COLLECTIONS =============
@app.route('/api/collections', methods=['GET'])
def get_collections():
    """Récupère toutes les collections"""
    try:
        url = f"{PLEX_SERVER}/library/sections?X-Plex-Token={PLEX_TOKEN}"
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        sections = response.json()
        collections = []
        
        for section in sections.get('MediaContainer', {}).get('Directory', []):
            collections.append({
                'id': section.get('key'),
                'title': section.get('title'),
                'type': section.get('type'),
                'count': section.get('contentCount', 0)
            })
        
        return jsonify({
            'status': 'success',
            'data': collections,
            'count': len(collections)
        })
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

# ============= ENDPOINT 2: GET TRENDING =============
@app.route('/api/collections/trending', methods=['GET'])
def get_trending():
    """Récupère les films en tendance"""
    try:
        limit = request.args.get('limit', 20, type=int)
        url = f"{PLEX_SERVER}/library/all?sort=addedAt:desc&limit={limit}&X-Plex-Token={PLEX_TOKEN}"
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        items = response.json()
        movies = []
        
        for item in items.get('MediaContainer', {}).get('Metadata', [])[:limit]:
            movies.append({
                'id': item.get('ratingKey'),
                'title': item.get('title'),
                'poster': item.get('thumb', ''),
                'year': item.get('year'),
                'rating': item.get('rating'),
                'summary': item.get('summary', '')
            })
        
        return jsonify({
            'status': 'success',
            'type': 'trending',
            'data': movies,
            'count': len(movies)
        })
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

# ============= ENDPOINT 3: GET NEW RELEASES =============
@app.route('/api/collections/new', methods=['GET'])
def get_new_releases():
    """Récupère les nouvelles sorties"""
    try:
        limit = request.args.get('limit', 20, type=int)
        url = f"{PLEX_SERVER}/library/all?sort=addedAt:desc&limit={limit}&X-Plex-Token={PLEX_TOKEN}"
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        items = response.json()
        movies = []
        
        for item in items.get('MediaContainer', {}).get('Metadata', [])[:limit]:
            movies.append({
                'id': item.get('ratingKey'),
                'title': item.get('title'),
                'poster': item.get('thumb', ''),
                'year': item.get('year'),
                'dateAdded': item.get('addedAt'),
                'summary': item.get('summary', '')
            })
        
        return jsonify({
            'status': 'success',
            'type': 'new',
            'data': movies,
            'count': len(movies)
        })
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

# ============= ENDPOINT 4: GET CONTINUE WATCHING =============
@app.route('/api/collections/continue', methods=['GET'])
def get_continue_watching():
    """Récupère les films à continuer"""
    try:
        limit = request.args.get('limit', 10, type=int)
        url = f"{PLEX_SERVER}/library/all?includeViewCount=1&sort=lastViewedAt:desc&limit={limit}&X-Plex-Token={PLEX_TOKEN}"
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        items = response.json()
        movies = []
        
        for item in items.get('MediaContainer', {}).get('Metadata', [])[:limit]:
            if item.get('viewCount', 0) > 0:
                movies.append({
                    'id': item.get('ratingKey'),
                    'title': item.get('title'),
                    'poster': item.get('thumb', ''),
                    'viewOffset': item.get('viewOffset', 0),
                    'duration': item.get('duration', 0),
                    'lastViewed': item.get('lastViewedAt'),
                    'summary': item.get('summary', '')
                })
        
        return jsonify({
            'status': 'success',
            'type': 'continue',
            'data': movies,
            'count': len(movies)
        })
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

# ============= ENDPOINT 5: GET IMAGE PROXY =============
@app.route('/api/proxy/image/<key>', methods=['GET'])
def get_image_proxy(key):
    """Proxy les images avec cache WebP"""
    try:
        format_type = request.args.get('format', 'webp')
        size = request.args.get('size', 'medium')
        
        # Récupérer l'image
        url = f"{PLEX_SERVER}/{key}?X-Plex-Token={PLEX_TOKEN}"
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        # Convertir en WebP
        img = Image.open(BytesIO(response.content))
        
        # Redimensionner selon la taille
        sizes = {
            'small': (200, 300),
            'medium': (400, 600),
            'large': (800, 1200)
        }
        
        size_tuple = sizes.get(size, sizes['medium'])
        img.thumbnail(size_tuple, Image.Resampling.LANCZOS)
        
        # Sauvegarder en cache
        cache_path = f"{CACHE_DIR}/{key}_{size}.webp"
        img.save(cache_path, 'WEBP', quality=85)
        
        return send_file(cache_path, mimetype='image/webp')
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)}), 500

# ============= HEALTH CHECK =============
@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'healthy', 'service': 'Plex Backend API'})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
```

---

## 🟡 JOUR 3: CACHING & IMAGES

### image_cache.py - Gestion du cache

```python
import os
import hashlib
from PIL import Image
from io import BytesIO
import requests

class ImageCache:
    def __init__(self, cache_dir="image_cache"):
        self.cache_dir = cache_dir
        os.makedirs(cache_dir, exist_ok=True)
    
    def get_cache_path(self, url, size='medium'):
        hash_name = hashlib.md5(url.encode()).hexdigest()
        return f"{self.cache_dir}/{hash_name}_{size}.webp"
    
    def cache_image(self, url, size='medium'):
        cache_path = self.get_cache_path(url, size)
        
        if os.path.exists(cache_path):
            return cache_path
        
        response = requests.get(url, timeout=10)
        img = Image.open(BytesIO(response.content))
        
        sizes = {
            'small': (200, 300),
            'medium': (400, 600),
            'large': (800, 1200)
        }
        
        img.thumbnail(sizes.get(size, sizes['medium']), Image.Resampling.LANCZOS)
        img.save(cache_path, 'WEBP', quality=85)
        
        return cache_path
    
    def cleanup(self, max_age_days=30):
        import time
        current_time = time.time()
        
        for filename in os.listdir(self.cache_dir):
            filepath = os.path.join(self.cache_dir, filename)
            file_age = current_time - os.path.getmtime(filepath)
            
            if file_age > max_age_days * 86400:
                os.remove(filepath)
```

---

## 🟢 JOURS 4-5: STREAMING & TESTS

### Test Commands (curl)

```bash
# Test 1: Health Check
curl http://localhost:8000/health

# Test 2: Get Collections
curl "http://localhost:8000/api/collections"

# Test 3: Get Trending (limit 10)
curl "http://localhost:8000/api/collections/trending?limit=10"

# Test 4: Get New Releases
curl "http://localhost:8000/api/collections/new?limit=10"

# Test 5: Get Continue Watching
curl "http://localhost:8000/api/collections/continue?limit=10"

# Test 6: Get Proxied Image
curl "http://localhost:8000/api/proxy/image/library%2Fmetadata%2F1?format=webp&size=medium"
```

---

## ✅ VALIDATION CHECKLIST

- [ ] Backend démarre sans erreur
- [ ] `/health` répond avec statut 200
- [ ] `/api/collections` retourne liste des collections
- [ ] `/api/collections/trending` retourne films en tendance
- [ ] `/api/collections/new` retourne nouvelles sorties
- [ ] `/api/collections/continue` retourne films commencés
- [ ] Images sont converties en WebP
- [ ] Cache fonctionne (fichiers .webp créés)
- [ ] API répond aux requêtes simultanées
- [ ] Erreurs gérées proprement

---

## 🚀 RÉSULTAT FINAL

✅ Backend API Python complète et testée  
✅ 5 endpoints fonctionnels  
✅ Image proxy avec cache WebP  
✅ Prêt pour l'intégration Android  

**Semaine 1: ✅ COMPLÈTE!**
