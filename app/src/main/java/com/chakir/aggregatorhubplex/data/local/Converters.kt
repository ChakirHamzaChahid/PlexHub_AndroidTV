package com.chakir.aggregatorhubplex.data.local

import androidx.room.TypeConverter
import com.chakir.aggregatorhubplex.domain.model.AudioTrack
import com.chakir.aggregatorhubplex.domain.model.Chapter
import com.chakir.aggregatorhubplex.domain.model.Marker
import com.chakir.aggregatorhubplex.domain.model.Season
import com.chakir.aggregatorhubplex.domain.model.Server
import com.chakir.aggregatorhubplex.domain.model.SimilarItem
import com.chakir.aggregatorhubplex.domain.model.Subtitle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Convertisseurs de types pour Room. Permet de stocker des objets complexes (Listes, Objets
 * personnalisés) dans des colonnes de base de données (généralement sous forme de chaînes JSON).
 * Utilise Kotlinx Serialization pour la sérialisation/désérialisation.
 */
class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // --- SERVEURS ---
    @TypeConverter
    fun fromServers(value: List<Server>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toServers(value: String): List<Server> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- SAISONS ---
    @TypeConverter
    fun fromSeasons(value: List<Season>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toSeasons(value: String): List<Season> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- GENRES (List<String>) ---
    @TypeConverter
    fun fromGenres(value: List<String>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toGenres(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- AUDIO TRACKS ---
    @TypeConverter
    fun fromAudioTracks(value: List<AudioTrack>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toAudioTracks(value: String): List<AudioTrack> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- SUBTITLES ---
    @TypeConverter
    fun fromSubtitles(value: List<Subtitle>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toSubtitles(value: String): List<Subtitle> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- CHAPTERS ---
    @TypeConverter
    fun fromChapters(value: List<Chapter>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toChapters(value: String): List<Chapter> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- MARKERS ---
    @TypeConverter
    fun fromMarkers(value: List<Marker>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toMarkers(value: String): List<Marker> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- SIMILAR ITEMS ---
    @TypeConverter
    fun fromSimilarItems(value: List<SimilarItem>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toSimilarItems(value: String): List<SimilarItem> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
