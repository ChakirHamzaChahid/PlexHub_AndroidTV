package com.chakir.aggregatorhubplex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de données Room principale de l'application. Contient les entités pour les films, les
 * favoris, l'historique de lecture et les index de recherche. Version 4 : Ajout de la table FTS4 et
 * des nouvelles entités.
 */
@Database(
        entities =
                [
                        MovieEntity::class,
                        MovieFtsEntity::class, // <-- NOUVELLE TABLE D'INDEX FTS4
                        RemoteKeys::class,
                        FavoriteEntity::class,
                        PlayHistoryEntity::class],
        version = 4, // <-- VERSION INCRÉMENTÉE POUR LA MIGRATION
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Récupère l'instance singleton de la base de données. Utilise un verrou (synchronized)
         * pour éviter la création multiple d'instances.
         *
         * @param context Contexte de l'application.
         * @return L'instance de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                AppDatabase::class.java,
                                                "plexhub_database"
                                        )
                                        .fallbackToDestructiveMigration() // Recrée la DB si la
                                        // version change et
                                        // qu'aucune migration
                                        // n'est trouvée
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}
