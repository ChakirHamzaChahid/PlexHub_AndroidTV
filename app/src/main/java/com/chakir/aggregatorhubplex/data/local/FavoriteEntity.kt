package com.chakir.aggregatorhubplex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaId: String,
    val type: String, // "movie" ou "show"
    val title: String, // Utile pour l'affichage hors ligne rapide
    val posterUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
)