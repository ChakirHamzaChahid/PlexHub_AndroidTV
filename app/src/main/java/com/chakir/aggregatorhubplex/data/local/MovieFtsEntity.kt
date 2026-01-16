package com.chakir.aggregatorhubplex.data.local

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Table virtuelle FTS4 pour l'indexation de recherche plein texte.
 * Elle est liée à MovieEntity et indexe les colonnes `title` et `description`.
 * FTS4 est utilisé pour sa simplicité de configuration.
 */
@Entity(tableName = "movies_fts")
@Fts4(contentEntity = MovieEntity::class)
data class MovieFtsEntity(
    val title: String,
    val description: String?
)
