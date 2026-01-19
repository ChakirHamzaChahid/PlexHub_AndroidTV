package com.chakir.aggregatorhubplex.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension pour créer le DataStore unique
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "plexhub_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // --- KEYS ---
    companion object {
        val SERVER_URL = stringPreferencesKey("server_url")
        val QUALITY = stringPreferencesKey("quality") // "auto", "1080p", "720p"
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val SHOW_ADULT_CONTENT = booleanPreferencesKey("show_adult_content")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val PREF_AUDIO_LANG = stringPreferencesKey("pref_audio_lang")
        val SORT_OPTION = stringPreferencesKey("sort_option") // ADDED
    }

    // --- READ (Flows) ---

    val serverUrl: Flow<String?> = dataStore.data.map { it[SERVER_URL] }

    val preferredQuality: Flow<String> = dataStore.data.map { it[QUALITY] ?: "auto" }

    val autoPlayNext: Flow<Boolean> = dataStore.data.map { it[AUTO_PLAY_NEXT] ?: true }

    val gridColumns: Flow<Int> = dataStore.data.map { it[GRID_COLUMNS] ?: 5 }

    val preferredAudioLang: Flow<String> = dataStore.data.map { it[PREF_AUDIO_LANG] ?: "fra" }

    val sortOption: Flow<String> = dataStore.data.map { it[SORT_OPTION] ?: "ADDED_DESC" }

    // --- WRITE (Suspending functions) ---

    suspend fun setServerUrl(url: String) {
        dataStore.edit { prefs -> prefs[SERVER_URL] = url }
    }

    suspend fun setQuality(quality: String) {
        dataStore.edit { prefs -> prefs[QUALITY] = quality }
    }

    suspend fun setAutoPlayNext(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[AUTO_PLAY_NEXT] = enabled }
    }

    suspend fun setGridColumns(cols: Int) {
        dataStore.edit { prefs -> prefs[GRID_COLUMNS] = cols }
    }

    suspend fun setAudioLanguage(lang: String) {
        dataStore.edit { prefs -> prefs[PREF_AUDIO_LANG] = lang }
    }

    suspend fun setSortOption(sort: String) {
        dataStore.edit { prefs -> prefs[SORT_OPTION] = sort }
    }

    // Reset complet (Déconnexion)
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
