package com.chakir.aggregatorhubplex.data

/**
 * Objet utilitaire définissant le regroupement des genres pour l'interface utilisateur. Permet de
 * mapper des catégories UI conviviales (ex: "Action & Aventure") vers des listes de mots-clés
 * techniques.
 */
object GenreGrouping {
    // La liste des catégories à afficher dans l'interface (les boutons)
    // Clé = Nom affiché, Valeur = Liste des mots-clés à chercher en base
    val GROUPS =
            mapOf(
                    "Tout" to emptyList(), // Pas de filtre
                    "Animation" to listOf("Animation", "Anime", "Japanimé", "Kaï", "Short"),
                    "Action & Aventure" to
                            listOf(
                                    "Action",
                                    "Adventure",
                                    "Aventure",
                                    "Martial Arts",
                                    "Action/Aventure"
                            ),
                    "Sci-Fi & Fantastique" to
                            listOf("Sci-Fi", "Science Fiction", "Fantasy", "Sci-Fi &amp; Fantasy"),
                    "Famille & Animaux" to listOf("Familial", "Family", "Children", "Enfants"),
                    "Comédie" to listOf("Comedy"),
                    "Drame & Romance" to listOf("Drama", "Romance", "Soap", "Adult"),
                    "Thriller & Horreur" to
                            listOf(
                                    "Thriller",
                                    "Horror",
                                    "Suspense",
                                    "Crime",
                                    "Mystery",
                                    "Film-Noir"
                            ),
                    "Guerre & Histoire" to listOf("War", "History", "Western", "Biography"),
                    "Documentaire & TV" to
                            listOf(
                                    "Documentary",
                                    "Reality",
                                    "Game Show",
                                    "Talk",
                                    "News",
                                    "Home And Garden",
                                    "Food",
                                    "Travel",
                                    "Sport",
                                    "Music",
                                    "Musical",
                                    "Indie"
                            )
            )

    // Juste les clés pour l'UI (pour faire la liste horizontale)
    val UI_LABELS = GROUPS.keys.toList()
}
