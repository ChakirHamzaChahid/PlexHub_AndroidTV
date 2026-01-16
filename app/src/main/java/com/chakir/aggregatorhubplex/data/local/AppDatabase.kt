package com.chakir.aggregatorhubplex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MovieEntity::class,
        RemoteKeys::class,
        FavoriteEntity::class, // <--- NOUVEAU (Table Favoris)
        PlayHistoryEntity::class // <--- NOUVEAU (Table Historique)
    ],
    version = 2, // <--- VERSION INCRÉMENTÉE (Force la migration destructive)
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    // --- NOUVEAUX DAOS ---
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plexhub_database"
                )
                    .fallbackToDestructiveMigration() // Reset la DB si la version change (v1 -> v2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}