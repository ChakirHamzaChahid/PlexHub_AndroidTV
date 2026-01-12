package com.chakir.aggregatorhubplex.data.local

import androidx.room.TypeConverter
import com.chakir.aggregatorhubplex.data.Season
import com.chakir.aggregatorhubplex.data.Server
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // --- SERVEURS ---
    @TypeConverter
    fun fromServers(value: List<Server>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toServers(value: String): List<Server> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) { emptyList() }
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
        } catch (e: Exception) { emptyList() }
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
        } catch (e: Exception) { emptyList() }
    }
}