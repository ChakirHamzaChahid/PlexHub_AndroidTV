package com.chakir.aggregatorhubplex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MovieEntity::class,
        MovieFtsEntity::class, // <-- NOUVELLE TABLE D'INDEX FTS4
        RemoteKeys::class,
        FavoriteEntity::class,
        PlayHistoryEntity::class
    ],
    version = 3, // <-- VERSION INCRÉMENTÉE POUR LA MIGRATION
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
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
                    .fallbackToDestructiveMigration() // Recrée la DB si la version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
